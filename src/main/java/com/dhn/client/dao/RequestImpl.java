package com.dhn.client.dao;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class RequestImpl implements RequestDAO {

    @Autowired
    private SqlSession sqlSession;

    ////////////////////////////// SUREDATA //////////////////////////////

    // KAO 발송 대기 건수 확인
    @Override
    public int selectKAORequestCount(SQLParameter param) throws Exception {
        int cnt = 0;
        cnt = sqlSession.selectOne("com.dhn.client.kakao.mapper.SendRequest.req_kao_count", param);
        return cnt;
    }

    // SMS 발송 대기 건수 확인
    @Override
    public int selectSMSRequestCount(SQLParameter param) throws Exception {
        int cnt = 0;
        cnt = sqlSession.selectOne("com.dhn.client.oracle.mapper.SendRequest.req_sms_count", param);
        return cnt;
    }

    // LMS 발송 대기 건수 확인
    @Override
    public int selectLMSRequestCount(SQLParameter param) throws Exception {
        int cnt = 0;
        cnt = sqlSession.selectOne("com.dhn.client.oracle.mapper.SendRequest.req_lms_count", param);
        return cnt;
    }

    // KAO 발송 상태 업테이트(그룹화, 1000건씩)
    @Override
    public void updateKAOGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_kao_group_update", param);
    }

    // SMS 발송 상태 업테이트(그룹화, 1000건씩)
    @Override
    public void updateSMSGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.oracle.mapper.SendRequest.req_sms_group_update", param);
    }

    // LMS 발송 상태 업테이트(그룹화, 1000건씩)
    @Override
    public void updateLMSGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.oracle.mapper.SendRequest.req_lms_group_update", param);
    }

    // KAO 발송 데이터 조회
    @Override
    public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.kakao.mapper.SendRequest.req_kao_select", param);
    }

    // SMS 발송 데이터 조회
    @Override
    public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.oracle.mapper.SendRequest.req_sms_select", param);
    }

    // LMS 발송 데이터 조회
    @Override
    public List<RequestBean> selectLMSRequests(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.oracle.mapper.SendRequest.req_lms_select", param);
    }

    // KAO 발송 완료 상태 변경
    @Override
    public void updateKAOSendComplete(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_sent_complete", param);
    }

    // SMS 발송 완료 상태 변경
    @Override
    public void updateSMSSendComplete(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.oracle.mapper.SendRequest.req_sent_complete", param);
    }

    // KAO 발송 상태 초기화
    @Override
    public void updateKAOSendInit(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_sent_init", param);
    }

    // SMS 발송 상태 초기화
    @Override
    public void updateSMSSendInit(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.oracle.mapper.SendRequest.req_sent_init", param);
    }

    // 결과 로그 처리
    @Override
    public void Insert_msg_log(Msg_Log _ml) throws Exception {
        sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert1", _ml);
        sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert2", _ml);
        sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert3", _ml);
    }


    ////////////////////////////// MMS_MSG //////////////////////////////

    // KAO 발송 대기 건수 확인
    @Override
    public int selectKAOMMSMSGRequestCount(SQLParameter param) throws Exception {
        int cnt = 0;
        cnt = sqlSession.selectOne("com.dhn.client.kakao_mms_msg.mapper.SendRequest.req_kao_count", param);
        return cnt;
    }

    // SMS 발송 대기 건수 확인
    @Override
    public int selectSMSMMSMSGRequestCount(SQLParameter param) throws Exception {
        int cnt = 0;
        cnt = sqlSession.selectOne("com.dhn.client.oracle_mms_msg.mapper.SendRequest.req_sms_count", param);
        return cnt;
    }

    // LMS 발송 대기 건수 확인
    @Override
    public int selectLMSMMSMSGRequestCount(SQLParameter param) throws Exception {
        int cnt = 0;
        cnt = sqlSession.selectOne("com.dhn.client.oracle_mms_msg.mapper.SendRequest.req_lms_count", param);
        return cnt;
    }

    // KAO 발송 상태 업테이트(그룹화, 1000건씩)
    @Override
    public void updateKAOMMSMSGGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.kakao_mms_msg.mapper.SendRequest.req_kao_group_update", param);
    }

    // SMS 발송 상태 업테이트(그룹화, 1000건씩)
    @Override
    public void updateSMSMMSMSGGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.oracle_mms_msg.mapper.SendRequest.req_sms_group_update", param);
    }

    // LMS 발송 상태 업테이트(그룹화, 1000건씩)
    @Override
    public void updateLMSMMSMSGGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.oracle_mms_msg.mapper.SendRequest.req_lms_group_update", param);
    }

    // KAO 발송 데이터 조회
    @Override
    public List<KAORequestBean> selectKAOMMSMSGRequests(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.kakao_mms_msg.mapper.SendRequest.req_kao_select", param);
    }

    // SMS 발송 데이터 조회
    @Override
    public List<RequestBean> selectSMSMMSMSGRequests(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.oracle_mms_msg.mapper.SendRequest.req_sms_select", param);
    }

    // LMS 발송 데이터 조회
    @Override
    public List<RequestBean> selectLMSMMSMSGRequests(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.oracle_mms_msg.mapper.SendRequest.req_lms_select", param);
    }

    // KAO 발송 완료 상태 변경
    @Override
    public void updateKAOMMSMSGSendComplete(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.kakao_mms_msg.mapper.SendRequest.req_sent_complete", param);
    }

    // SMS 발송 완료 상태 변경
    @Override
    public void updateSMSMMSMSGSendComplete(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.oracle_mms_msg.mapper.SendRequest.req_sent_complete", param);
    }

    // KAO 발송 상태 초기화
    @Override
    public void updateKAOMMSMSGSendInit(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.kakao_mms_msg.mapper.SendRequest.req_sent_init", param);
    }

    // SMS 발송 상태 초기화
    @Override
    public void updateSMSMMSMSGSendInit(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.oracle_mms_msg.mapper.SendRequest.req_sent_init", param);
    }

    // 결과 로그 처리
    @Override
    public void Insert_msg_log_MMS_MSG(Msg_Log _ml) throws Exception {
        sqlSession.update("com.dhn.client.kakao_mms_msg.mapper.SendRequest.result_log_insert1", _ml);
        sqlSession.update("com.dhn.client.kakao_mms_msg.mapper.SendRequest.result_log_insert2", _ml);
        sqlSession.update("com.dhn.client.kakao_mms_msg.mapper.SendRequest.result_log_insert3", _ml);
    }

    @Override
    public void logTableCheck(SQLParameter param) throws Exception{
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

        String lastMonth = now.minusMonths(1).format(formatter);
        String currentMonth = now.format(formatter);
        String nextMonth = now.plusMonths(1).format(formatter);

        String logTableLast = param.getLog_table()+"_"+lastMonth;
        String logTableCurrent = param.getLog_table()+"_"+currentMonth;
        String logTableNext = param.getLog_table()+"_"+nextMonth;

        Map<String, String> map = new HashMap<>();
        map.put("msgTable", param.getMsg_table());
        map.put("logTable",logTableLast);
        int result_last = sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.logTableCheck", map);
        if(result_last == 0){
            sqlSession.update("com.dhn.client.create.mapper.SendRequest.createLogTable", map);
            log.info("{} 테이블 생성",map.get("logTable"));
        }

        map.put("logTable",logTableCurrent);
        int result_current = sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.logTableCheck", map);
        if(result_current == 0){
            sqlSession.update("com.dhn.client.create.mapper.SendRequest.createLogTable", map);
            log.info("{} 테이블 생성",map.get("logTable"));

        }

        map.put("logTable",logTableNext);
        int result_next = sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.logTableCheck", map);
        if(result_next == 0){
            sqlSession.update("com.dhn.client.create.mapper.SendRequest.createLogTable", map);
            log.info("{} 테이블 생성",map.get("logTable"));

        }

        // 2
        logTableLast = param.getTran_log_table()+lastMonth;
        logTableCurrent = param.getTran_log_table()+currentMonth;
        logTableNext = param.getTran_log_table()+nextMonth;

        map = new HashMap<>();
        map.put("msgTable", param.getTran_msg_table());
        map.put("logTable",logTableLast);
        result_last = sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.logTableCheck", map);
        if(result_last == 0){
            sqlSession.update("com.dhn.client.create.mapper.SendRequest.createLogTable", map);
            log.info("{} 테이블 생성",map.get("logTable"));
        }

        map.put("logTable",logTableCurrent);
        result_current = sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.logTableCheck", map);
        if(result_current == 0){
            sqlSession.update("com.dhn.client.create.mapper.SendRequest.createLogTable", map);
            log.info("{} 테이블 생성",map.get("logTable"));

        }

        map.put("logTable",logTableNext);
        result_next = sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.logTableCheck", map);
        if(result_next == 0){
            sqlSession.update("com.dhn.client.create.mapper.SendRequest.createLogTable", map);
            log.info("{} 테이블 생성",map.get("logTable"));

        }

        // 3
        logTableLast = param.getMms_log_table()+lastMonth;
        logTableCurrent = param.getMms_log_table()+currentMonth;
        logTableNext = param.getMms_log_table()+nextMonth;

        map = new HashMap<>();
        map.put("msgTable", param.getMms_msg_table());
        map.put("logTable",logTableLast);
        result_last = sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.logTableCheck", map);
        if(result_last == 0){
            sqlSession.update("com.dhn.client.create.mapper.SendRequest.createLogTable", map);
            log.info("{} 테이블 생성",map.get("logTable"));
        }

        map.put("logTable",logTableCurrent);
        result_current = sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.logTableCheck", map);
        if(result_current == 0){
            sqlSession.update("com.dhn.client.create.mapper.SendRequest.createLogTable", map);
            log.info("{} 테이블 생성",map.get("logTable"));

        }

        map.put("logTable",logTableNext);
        result_next = sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.logTableCheck", map);
        if(result_next == 0){
            sqlSession.update("com.dhn.client.create.mapper.SendRequest.createLogTable", map);
            log.info("{} 테이블 생성",map.get("logTable"));

        }
    }

    // tran 통일
    @Override
    public int selectTranRequestCount(SQLParameter param) throws Exception {
        int cnt = 0;
        cnt = sqlSession.selectOne("com.dhn.client.tran.mapper.SendRequest.req_tran_count", param);
        return cnt;
    }

    @Override
    public void updateTranGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.tran.mapper.SendRequest.req_tran_group_update", param);
    }

    @Override
    public List<RequestBean> selectTranRequests(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.tran.mapper.SendRequest.req_tran_select", param);
    }

    @Override
    public void updateTranSendComplete(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.tran.mapper.SendRequest.req_tran_sent_complete", param);
    }

    @Override
    public void updateTranSendInit(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.tran.mapper.SendRequest.req_tran_sent_init", param);
    }

    @Override
    public void Insert_msg_log_Tran(Msg_Log _ml) throws Exception {
        sqlSession.update("com.dhn.client.tran.mapper.SendRequest.result_log_insert1", _ml);
        sqlSession.update("com.dhn.client.tran.mapper.SendRequest.result_log_insert2", _ml);
        sqlSession.update("com.dhn.client.tran.mapper.SendRequest.result_log_insert3", _ml);
    }
}
