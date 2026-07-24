package com.dhn.client.dao;

import com.dhn.client.bean.ImageBean;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Slf4j
public class MSGRequestDAOimpl implements MSGRequestDAO{

    @Autowired
    private SqlSession sqlSession;

    @Override
    public int selectSMSRequestCount(SQLParameter param) throws Exception {
        return sqlSession.selectOne("com.dhn.client.msg.mapper.SendRequest.sms_count",param);
    }

    @Override
    public void updateSMSGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.msg.mapper.SendRequest.sms_group_update",param);
    }

    @Override
    public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.msg.mapper.SendRequest.sms_select",param);
    }

    @Override
    public int selectLMSRequestCount(SQLParameter param) throws Exception {
        return sqlSession.selectOne("com.dhn.client.msg.mapper.SendRequest.lms_count",param);
    }

    @Override
    public void updateLMSGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.msg.mapper.SendRequest.lms_group_update",param);
    }

    @Override
    public List<RequestBean> selectLMSRequests(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.msg.mapper.SendRequest.lms_select",param);
    }

    @Override
    public int selectMMSRequestCount(SQLParameter param) throws Exception {
        return sqlSession.selectOne("com.dhn.client.msg.mapper.SendRequest.mms_count",param);
    }

    @Override
    public void updateMMSGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.msg.mapper.SendRequest.mms_group_update",param);
    }

    @Override
    public List<RequestBean> selectMMSRequests(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.msg.mapper.SendRequest.mms_select",param);
    }

    @Override
    public int selectMMSImageCount(SQLParameter param) throws Exception {
        return sqlSession.selectOne("com.dhn.client.msg.mapper.SendRequest.mms_image_count",param);
    }

    @Override
    public void updateMMSImageGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.msg.mapper.SendRequest.mms_image_group_update",param);
    }

    @Override
    public List<ImageBean> selectMMSImage(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.msg.mapper.SendRequest.mms_image_select", param);
    }

    @Override
    public void updateMMSImageGroup(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.msg.mapper.SendRequest.mms_key_update", param);
    }

    @Override
    public void updateMMSImageFail(SQLParameter param) throws Exception {

        int retry = 0;
        int maxRetry = 5;

        while (true) {
            try {
                ((MSGRequestDAO) AopContext.currentProxy()).doupdateMMSImageFailTx(param);
                return;
            } catch (Exception e) {
                retry++;
                log.warn("[RETRY] updateMMSImageFail retry {}/{} msgid={} / {}",retry,maxRetry,param.getMsgid(),e);
                if (!isRetryable(e) || retry >= maxRetry) {
                    log.error("[FAIL] updateMMSImageFail failed after {} retries", retry, e);
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
    public void doupdateMMSImageFailTx(SQLParameter param) {
        sqlSession.update("com.dhn.client.msg.mapper.SendRequest.mms_image_fail_update",param);
        sqlSession.update("com.dhn.client.msg.mapper.SendRequest.mms_image_fail_log_insert", param);
        sqlSession.update("com.dhn.client.msg.mapper.SendRequest.mms_image_fail_delete", param);
    }

    @Override
    public void updateMsgSendComplete(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.msg.mapper.SendRequest.msg_sent_complete",param);
    }

    @Override
    public void updateMsgSendInit(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.msg.mapper.SendRequest.msg_sent_init",param);
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

                // SQLState 확인 (MSSQL, MySQL 등 공통 40001 등)
                String state = sqlEx.getSQLState();
                if ("40001".equals(state)) {
                    return true;
                }

                // MSSQL 에러 번호 확인 (1205: 데드락, 1222: 락 타임아웃)
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
