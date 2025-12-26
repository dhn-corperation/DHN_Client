package com.dhn.client.dao;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;

import java.util.List;

public interface RequestDAO {

    ////////////////////////////// SUREDATA //////////////////////////////

    // 발송 대기 건수 확인
    public int selectKAORequestCount(SQLParameter param) throws Exception;
    public int selectSMSRequestCount(SQLParameter param) throws Exception;
    public int selectLMSRequestCount(SQLParameter param) throws Exception;

    // 발송 상태 업테이트(그룹화, 1000건씩)
    public void updateKAOGroupNo(SQLParameter param) throws Exception;
    public void updateSMSGroupNo(SQLParameter param) throws Exception;
    public void updateLMSGroupNo(SQLParameter param) throws Exception;

    // 발송 데이터 조회
    public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception;
    public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception;
    public List<RequestBean> selectLMSRequests(SQLParameter param) throws Exception;

    // 발송 완료 상태 변경
    public void updateKAOSendComplete(SQLParameter param) throws Exception;
    public void updateSMSSendComplete(SQLParameter param) throws Exception;

    // 발송 상태 초기화
    public void updateKAOSendInit(SQLParameter param) throws Exception;
    public void updateSMSSendInit(SQLParameter param) throws Exception;

    // 결과 처리
    public void Insert_msg_log(Msg_Log _ml) throws Exception;

    ////////////////////////////// TRAN //////////////////////////////

    // 발송 대기 건수 확인
    public int selectKAOTranRequestCount(SQLParameter param) throws Exception;
    public int selectSMSTranRequestCount(SQLParameter param) throws Exception;
    public int selectLMSTranRequestCount(SQLParameter param) throws Exception;

    // 발송 상태 업테이트(그룹화, 1000건씩)
    public void updateKAOTranGroupNo(SQLParameter param) throws Exception;
    public void updateSMSTranGroupNo(SQLParameter param) throws Exception;
    public void updateLMSTranGroupNo(SQLParameter param) throws Exception;

    // 발송 데이터 조회
    public List<KAORequestBean> selectKAOTranRequests(SQLParameter param) throws Exception;
    public List<RequestBean> selectSMSTranRequests(SQLParameter param) throws Exception;
    public List<RequestBean> selectLMSTranRequests(SQLParameter param) throws Exception;

    // 발송 완료 상태 변경
    public void updateKAOTranSendComplete(SQLParameter param) throws Exception;
    public void updateSMSTranSendComplete(SQLParameter param) throws Exception;

    // 발송 상태 초기화
    public void updateKAOTranSendInit(SQLParameter param) throws Exception;
    public void updateSMSTranSendInit(SQLParameter param) throws Exception;

    // 결과 처리
    public void Insert_msg_log_Tran(Msg_Log _ml) throws Exception;

    ////////////////////////////// MMS_MSG //////////////////////////////

    // 발송 대기 건수 확인
    public int selectKAOMMSMSGRequestCount(SQLParameter param) throws Exception;
    public int selectSMSMMSMSGRequestCount(SQLParameter param) throws Exception;
    public int selectLMSMMSMSGRequestCount(SQLParameter param) throws Exception;

    // 발송 상태 업테이트(그룹화, 1000건씩)
    public void updateKAOMMSMSGGroupNo(SQLParameter param) throws Exception;
    public void updateSMSMMSMSGGroupNo(SQLParameter param) throws Exception;
    public void updateLMSMMSMSGGroupNo(SQLParameter param) throws Exception;

    // 발송 데이터 조회
    public List<KAORequestBean> selectKAOMMSMSGRequests(SQLParameter param) throws Exception;
    public List<RequestBean> selectSMSMMSMSGRequests(SQLParameter param) throws Exception;
    public List<RequestBean> selectLMSMMSMSGRequests(SQLParameter param) throws Exception;

    // 발송 완료 상태 변경
    public void updateKAOMMSMSGSendComplete(SQLParameter param) throws Exception;
    public void updateSMSMMSMSGSendComplete(SQLParameter param) throws Exception;

    // 발송 상태 초기화
    public void updateKAOMMSMSGSendInit(SQLParameter param) throws Exception;
    public void updateSMSMMSMSGSendInit(SQLParameter param) throws Exception;

    // 결과 처리
    public void Insert_msg_log_MMS_MSG(Msg_Log _ml) throws Exception;
    
    // 로그테이블 생성
    public void logTableCheck(SQLParameter param) throws Exception;
}
