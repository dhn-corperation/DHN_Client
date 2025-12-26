package com.dhn.client.service;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.dao.RequestDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestServiceImpl implements RequestService {

    @Autowired
    private RequestDAO requestDAO;

    ////////////////////////////// SUREDATA //////////////////////////////

    // KAO 발송 대기 건수 확인
    public int selectKAORequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectKAORequestCount(param);
    }

    // SMS 발송 대기 건수 확인
    public int selectSMSRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectSMSRequestCount(param);
    }

    // LMS 발송 대기 건수 확인
    public int selectLMSRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectLMSRequestCount(param);
    }

    // KAO 발송 상태 업테이트(그룹화, 1000건씩)
    public void updateKAOGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateKAOGroupNo(param);
    }

    // SMS 발송 상태 업테이트(그룹화, 1000건씩)
    public void updateSMSGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateSMSGroupNo(param);
    }

    // LMS 발송 상태 업테이트(그룹화, 1000건씩)
    public void updateLMSGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateLMSGroupNo(param);
    }

    // KAO 발송 데이터 조회
    public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {
        return requestDAO.selectKAORequests(param);
    }

    // SMS 발송 데이터 조회
    public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception {
        return requestDAO.selectSMSRequests(param);
    }

    // LMS 발송 데이터 조회
    public List<RequestBean> selectLMSRequests(SQLParameter param) throws Exception {
        return requestDAO.selectLMSRequests(param);
    }

    // KAO 발송 완료 상태 변경
    public void updateKAOSendComplete(SQLParameter param) throws Exception {
        requestDAO.updateKAOSendComplete(param);
    }

    // SMS 발송 완료 상태 변경
    public void updateSMSSendComplete(SQLParameter param) throws Exception {
        requestDAO.updateSMSSendComplete(param);
    }

    // KAO 발송 상태 초기화
    public void updateKAOSendInit(SQLParameter param) throws Exception {
        requestDAO.updateKAOSendInit(param);
    }

    // SMS 발송 상태 초기화
    public void updateSMSSendInit(SQLParameter param) throws Exception {
        requestDAO.updateSMSSendInit(param);
    }

    // 결과 로그 처리
    public void Insert_msg_log(Msg_Log _ml) throws Exception {
        requestDAO.Insert_msg_log(_ml);
    }

    ////////////////////////////// TRAN //////////////////////////////

    // KAO 발송 대기 건수 확인
    public int selectKAOTranRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectKAOTranRequestCount(param);
    }

    // SMS 발송 대기 건수 확인
    public int selectSMSTranRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectSMSTranRequestCount(param);
    }

    // LMS 발송 대기 건수 확인
    public int selectLMSTranRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectLMSTranRequestCount(param);
    }

    // KAO 발송 상태 업테이트(그룹화, 1000건씩)
    public void updateKAOTranGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateKAOTranGroupNo(param);
    }

    // SMS 발송 상태 업테이트(그룹화, 1000건씩)
    public void updateSMSTranGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateSMSTranGroupNo(param);
    }

    // LMS 발송 상태 업테이트(그룹화, 1000건씩)
    public void updateLMSTranGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateLMSTranGroupNo(param);
    }

    // KAO 발송 데이터 조회
    public List<KAORequestBean> selectKAOTranRequests(SQLParameter param) throws Exception {
        return requestDAO.selectKAOTranRequests(param);
    }

    // SMS 발송 데이터 조회
    public List<RequestBean> selectSMSTranRequests(SQLParameter param) throws Exception {
        return requestDAO.selectSMSTranRequests(param);
    }

    // LMS 발송 데이터 조회
    public List<RequestBean> selectLMSTranRequests(SQLParameter param) throws Exception {
        return requestDAO.selectLMSTranRequests(param);
    }

    // KAO 발송 완료 상태 변경
    public void updateKAOTranSendComplete(SQLParameter param) throws Exception {
        requestDAO.updateKAOTranSendComplete(param);
    }

    // SMS 발송 완료 상태 변경
    public void updateSMSTranSendComplete(SQLParameter param) throws Exception {
        requestDAO.updateSMSTranSendComplete(param);
    }

    // KAO 발송 상태 초기화
    public void updateKAOTranSendInit(SQLParameter param) throws Exception {
        requestDAO.updateKAOTranSendInit(param);
    }

    // SMS 발송 상태 초기화
    public void updateSMSTranSendInit(SQLParameter param) throws Exception {
        requestDAO.updateSMSTranSendInit(param);
    }

    // 결과 로그 처리
    public void Insert_msg_log_Tran(Msg_Log _ml) throws Exception {
        requestDAO.Insert_msg_log_Tran(_ml);
    }

    ////////////////////////////// MMS_MSG //////////////////////////////

    // KAO 발송 대기 건수 확인
    public int selectKAOMMSMSGRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectKAOMMSMSGRequestCount(param);
    }

    // SMS 발송 대기 건수 확인
    public int selectSMSMMSMSGRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectSMSMMSMSGRequestCount(param);
    }

    // LMS 발송 대기 건수 확인
    public int selectLMSMMSMSGRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectLMSMMSMSGRequestCount(param);
    }

    // KAO 발송 상태 업테이트(그룹화, 1000건씩)
    public void updateKAOMMSMSGGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateKAOMMSMSGGroupNo(param);
    }

    // SMS 발송 상태 업테이트(그룹화, 1000건씩)
    public void updateSMSMMSMSGGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateSMSMMSMSGGroupNo(param);
    }

    // LMS 발송 상태 업테이트(그룹화, 1000건씩)
    public void updateLMSMMSMSGGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateLMSMMSMSGGroupNo(param);
    }

    // KAO 발송 데이터 조회
    public List<KAORequestBean> selectKAOMMSMSGRequests(SQLParameter param) throws Exception {
        return requestDAO.selectKAOMMSMSGRequests(param);
    }

    // SMS 발송 데이터 조회
    public List<RequestBean> selectSMSMMSMSGRequests(SQLParameter param) throws Exception {
        return requestDAO.selectSMSMMSMSGRequests(param);
    }

    // LMS 발송 데이터 조회
    public List<RequestBean> selectLMSMMSMSGRequests(SQLParameter param) throws Exception {
        return requestDAO.selectLMSMMSMSGRequests(param);
    }

    // KAO 발송 완료 상태 변경
    public void updateKAOMMSMSGSendComplete(SQLParameter param) throws Exception {
        requestDAO.updateKAOMMSMSGSendComplete(param);
    }

    // SMS 발송 완료 상태 변경
    public void updateSMSMMSMSGSendComplete(SQLParameter param) throws Exception {
        requestDAO.updateSMSMMSMSGSendComplete(param);
    }

    // KAO 발송 상태 초기화
    public void updateKAOMMSMSGSendInit(SQLParameter param) throws Exception {
        requestDAO.updateKAOMMSMSGSendInit(param);
    }

    // SMS 발송 상태 초기화
    public void updateSMSMMSMSGSendInit(SQLParameter param) throws Exception {
        requestDAO.updateSMSMMSMSGSendInit(param);
    }

    // 결과 로그 처리
    public void Insert_msg_log_MMS_MSG(Msg_Log _ml) throws Exception {
        requestDAO.Insert_msg_log_MMS_MSG(_ml);
    }

    // 로그테이블 생성
    public void logTableCheck(SQLParameter param) throws Exception{
        requestDAO.logTableCheck(param);
    }


}