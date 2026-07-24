package com.dhn.client.dao;

import com.dhn.client.bean.BMDataBean;
import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.SQLParameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class BMRequestDAOimpl implements BMRequestDAO {

    @Autowired
    private SqlSession sqlSession;

    @Override
    public int selectBMRequestCount(SQLParameter param) throws Exception {
        int cnt = 0;
        cnt = sqlSession.selectOne("com.dhn.client.brand.mapper.SendRequest.bm_count",param);
        return cnt;
    }

    @Override
    public void updateBMGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.brand.mapper.SendRequest.bm_group_update",param);
    }

    @Override
    public List<BMDataBean> selectBMRequests(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.brand.mapper.SendRequest.bm_select", param);
    }

    @Override
    public void updateBMSendComplete(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.brand.mapper.SendRequest.bm_sent_complete", param);
    }

    @Override
    public void updateBMSendInit(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.brand.mapper.SendRequest.bm_sent_init", param);
    }

    @Override
    public void updateInvalidData(List<String> invalidList, Msg_Log ml) throws Exception {
        int retry = 0;
        int maxRetry = 5;

        while (true) {

            try {
                ((BMRequestDAO) AopContext.currentProxy()).doUpdateInvalidDataTx(invalidList, ml);
                return;
            } catch (Exception e) {
                retry++;
                log.warn("[RETRY] updateInvalidData retry {}/{} invalidCnt={} / {}",retry,maxRetry,invalidList.size(),e);
                if (!isRetryable(e) || retry >= maxRetry) {
                    log.error("[FAIL] updateInvalidData failed after {} retries", retry, e);
                    throw e;
                }
                Thread.sleep(200 * retry);
            }
        }
    }

    @Override
    @Transactional(
            rollbackFor = Exception.class,
            propagation = Propagation.REQUIRES_NEW
    )
    public void doUpdateInvalidDataTx(List<String> invalidList, Msg_Log ml) {

        Map<String, Object> param = new HashMap<>();
        param.put("list", invalidList);
        param.put("ml", ml);

        sqlSession.update("com.dhn.client.brand.mapper.SendRequest.bm_invalid_update", param);
        sqlSession.insert("com.dhn.client.brand.mapper.SendRequest.bm_invalid_log_insert", param);
        sqlSession.delete("com.dhn.client.brand.mapper.SendRequest.bm_invalid_delete", param);
    }

    private boolean isRetryable(Exception e) {
        Throwable t = e;
        while (t != null) {
            String msg = t.getMessage();
            if (msg != null) {
                msg = msg.toLowerCase();
                if (msg.contains("deadlock")
                        || msg.contains("lock wait timeout")) {
                    return true;
                }
                if (msg.contains("ora-00060")
                        || msg.contains("ora-30006")) {
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
}
