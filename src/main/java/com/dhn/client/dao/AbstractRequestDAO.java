package com.dhn.client.dao;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractRequestDAO {

    @Autowired
    protected SqlSession sqlSession;

    protected abstract String getNamespace();

    public int selectRequestCount(SQLParameter param) throws Exception {
        return sqlSession.selectOne(getNamespace() + ".count", param);
    }

    public void updateGroupNo(SQLParameter param) throws Exception {
        sqlSession.update(getNamespace() + ".group_update", param);
    }

    public List<RequestBean> selectRequests(SQLParameter param) throws Exception {
        return sqlSession.selectList(getNamespace() + ".select", param);
    }

    public void updateSendComplete(SQLParameter param) throws Exception {
        sqlSession.update(getNamespace() + ".sent_complete", param);
    }

    public void updateSendInit(SQLParameter param) throws Exception {
        sqlSession.update(getNamespace() + ".sent_init", param);
    }

    public void updateInvalidData(List<String> invalidList, Msg_Log ml) throws Exception {
        int retry = 0;
        int maxRetry = 5;
        while (true) {
            try {
                ((AbstractRequestDAO) AopContext.currentProxy()).doUpdateInvalidDataTx(invalidList, ml);
                return;
            } catch (Exception e) {
                retry++;
                log.warn("[RETRY] updateInvalidData retry {}/{} invalidCnt={}", retry, maxRetry, invalidList.size(), e);
                if (!isRetryable(e) || retry >= maxRetry) {
                    log.error("[FAIL] updateInvalidData failed", e);
                    throw e;
                }
                Thread.sleep(200 * retry);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void doUpdateInvalidDataTx(List<String> invalidList, Msg_Log ml) {
        Map<String, Object> param = new HashMap<>();
        param.put("list", invalidList);
        param.put("ml", ml);

        sqlSession.update(getNamespace() + ".invalid_update", param);
        sqlSession.insert(getNamespace() + ".invalid_log_insert", param);
        sqlSession.delete(getNamespace() + ".invalid_delete", param);
    }

    private boolean isRetryable(Exception e) {
        Throwable t = e;
        while (t != null) {
            String msg = t.getMessage();
            if (msg != null) {
                msg = msg.toLowerCase();
                if (msg.contains("deadlock") || msg.contains("lock wait timeout")) {
                    return true;
                }
                if (msg.contains("ora-00060") || msg.contains("ora-30006")) {
                    return true;
                }
            }

            if (t instanceof java.sql.SQLException) {
                java.sql.SQLException sqlEx = (java.sql.SQLException) t;

                String state = sqlEx.getSQLState();
                if ("40001".equals(state)) {
                    return true;
                }

                int errorCode = sqlEx.getErrorCode();
                if (errorCode == 1205 || errorCode == 1222) {
                    return true;
                }
            }
            t = t.getCause();
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyResultProcess(Msg_Log ml) throws Exception {
        sqlSession.update(getNamespace() + ".result_update", ml);

        sqlSession.insert(getNamespace() + ".result_log_insert", ml);

        sqlSession.delete(getNamespace() + ".result_delete", ml);
    }
}