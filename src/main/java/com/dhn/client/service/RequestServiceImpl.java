package com.dhn.client.service;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.dao.RequestDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RequestServiceImpl implements RequestService {

    @Autowired
    private RequestDAO requestDAO;

    ////////////////////////////// SUREDATA //////////////////////////////

    // KAO 발송 대기 건수 확인
    @Override
    public int selectKAORequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectKAORequestCount(param);
    }

    // SMS 발송 대기 건수 확인
    @Override
    public int selectSMSRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectSMSRequestCount(param);
    }

    // LMS 발송 대기 건수 확인
    @Override
    public int selectLMSRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectLMSRequestCount(param);
    }

    // KAO 발송 상태 업테이트(그룹화, 1000건씩)
    @Override
    public void updateKAOGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateKAOGroupNo(param);
    }

    // SMS 발송 상태 업테이트(그룹화, 1000건씩)
    @Override
    public void updateSMSGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateSMSGroupNo(param);
    }

    // LMS 발송 상태 업테이트(그룹화, 1000건씩)
    @Override
    public void updateLMSGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateLMSGroupNo(param);
    }

    // KAO 발송 데이터 조회
    @Override
    public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {
        return requestDAO.selectKAORequests(param);
    }

    // SMS 발송 데이터 조회
    @Override
    public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception {
        return requestDAO.selectSMSRequests(param);
    }

    // LMS 발송 데이터 조회
    @Override
    public List<RequestBean> selectLMSRequests(SQLParameter param) throws Exception {
        return requestDAO.selectLMSRequests(param);
    }

    // KAO 발송 완료 상태 변경
    @Override
    public void updateKAOSendComplete(SQLParameter param) throws Exception {
        requestDAO.updateKAOSendComplete(param);
    }

    // SMS 발송 완료 상태 변경
    @Override
    public void updateSMSSendComplete(SQLParameter param) throws Exception {
        requestDAO.updateSMSSendComplete(param);
    }

    // KAO 발송 상태 초기화
    @Override
    public void updateKAOSendInit(SQLParameter param) throws Exception {
        requestDAO.updateKAOSendInit(param);
    }

    // SMS 발송 상태 초기화
    @Override
    public void updateSMSSendInit(SQLParameter param) throws Exception {
        requestDAO.updateSMSSendInit(param);
    }

    // 결과 로그 처리
    @Override
    public void Insert_msg_log(Msg_Log _ml) throws Exception {
        requestDAO.Insert_msg_log(_ml);
    }

    ////////////////////////////// MMS_MSG //////////////////////////////

    // KAO 발송 대기 건수 확인
    @Override
    public int selectKAOMMSMSGRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectKAOMMSMSGRequestCount(param);
    }

    // SMS 발송 대기 건수 확인
    @Override
    public int selectSMSMMSMSGRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectSMSMMSMSGRequestCount(param);
    }

    // LMS 발송 대기 건수 확인
    @Override
    public int selectLMSMMSMSGRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectLMSMMSMSGRequestCount(param);
    }

    // KAO 발송 상태 업테이트(그룹화, 1000건씩)
    @Override
    public void updateKAOMMSMSGGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateKAOMMSMSGGroupNo(param);
    }

    // SMS 발송 상태 업테이트(그룹화, 1000건씩)
    @Override
    public void updateSMSMMSMSGGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateSMSMMSMSGGroupNo(param);
    }

    // LMS 발송 상태 업테이트(그룹화, 1000건씩)
    @Override
    public void updateLMSMMSMSGGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateLMSMMSMSGGroupNo(param);
    }

    // KAO 발송 데이터 조회
    @Override
    public List<KAORequestBean> selectKAOMMSMSGRequests(SQLParameter param) throws Exception {
        return requestDAO.selectKAOMMSMSGRequests(param);
    }

    // SMS 발송 데이터 조회
    @Override
    public List<RequestBean> selectSMSMMSMSGRequests(SQLParameter param) throws Exception {
        return requestDAO.selectSMSMMSMSGRequests(param);
    }

    // LMS 발송 데이터 조회
    @Override
    public List<RequestBean> selectLMSMMSMSGRequests(SQLParameter param) throws Exception {
        return requestDAO.selectLMSMMSMSGRequests(param);
    }

    // KAO 발송 완료 상태 변경
    @Override
    public void updateKAOMMSMSGSendComplete(SQLParameter param) throws Exception {
        requestDAO.updateKAOMMSMSGSendComplete(param);
    }

    // SMS 발송 완료 상태 변경
    @Override
    public void updateSMSMMSMSGSendComplete(SQLParameter param) throws Exception {
        requestDAO.updateSMSMMSMSGSendComplete(param);
    }

    // KAO 발송 상태 초기화
    @Override
    public void updateKAOMMSMSGSendInit(SQLParameter param) throws Exception {
        requestDAO.updateKAOMMSMSGSendInit(param);
    }

    // SMS 발송 상태 초기화
    @Override
    public void updateSMSMMSMSGSendInit(SQLParameter param) throws Exception {
        requestDAO.updateSMSMMSMSGSendInit(param);
    }

    // 결과 로그 처리
    @Override
    public void Insert_msg_log_MMS_MSG(Msg_Log _ml) throws Exception {
        requestDAO.Insert_msg_log_MMS_MSG(_ml);
    }

    // 로그테이블 생성
    @Override
    public void logTableCheck(SQLParameter param) throws Exception{
        requestDAO.logTableCheck(param);
    }


    // Tran 통일
    @Override
    public int selectTranRequestCount(SQLParameter param) throws Exception {
        return requestDAO.selectTranRequestCount(param);
    }

    @Override
    public void updateTranGroupNo(SQLParameter param) throws Exception {
        requestDAO.updateTranGroupNo(param);
    }

    @Override
    public List<RequestBean> selectTranRequests(SQLParameter param) throws Exception {
        return requestDAO.selectTranRequests(param);
    }

    @Override
    public void updateTranSendComplete(SQLParameter param) throws Exception {
        requestDAO.updateTranSendComplete(param);
    }

    @Override
    public void updateTranSendInit(SQLParameter param) throws Exception {
        requestDAO.updateTranSendInit(param);
    }

    @Override
    public void Insert_msg_log_Tran(Msg_Log _ml) throws Exception {
        requestDAO.Insert_msg_log_Tran(_ml);
    }


}