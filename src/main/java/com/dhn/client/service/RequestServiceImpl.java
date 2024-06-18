package com.dhn.client.service;

import java.util.Collections;
import java.util.List;

import com.dhn.client.bean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dhn.client.dao.RequestDAO;

@Service
public class RequestServiceImpl implements RequestService {

	@Autowired
	private RequestDAO requestDAO;

	@Override
	public int selectKAORequestCount(SQLParameter param) throws Exception {
		return requestDAO.selectKAORequestCount(param);
	}

	@Override
	public void updateKAOGroupNo(SQLParameter param) throws Exception {
		requestDAO.updateKAOGroupNo(param);
	}

	@Override
	public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {
		return requestDAO.selectKAORequests(param);
	}

	@Override
	public void updateKAOSendComplete(SQLParameter param) throws Exception {
		requestDAO.updateKAOSendComplete(param);
	}

	@Override
	public void updateKAOSendInit(SQLParameter param) throws Exception {
		requestDAO.updateKAOSendInit(param);
	}

	@Override
	public int selectSMSReqeustCount(SQLParameter param) throws Exception {
		return requestDAO.selectSMSReqeustCount(param);
	}

	@Override
	public void updateSMSGroupNo(SQLParameter param) throws Exception {
		requestDAO.updateSMSGroupNo(param);
	}

	@Override
	public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception {
		return requestDAO.selectSMSRequests(param);
	}

	@Override
	public void updateSMSSendComplete(SQLParameter param) throws Exception {
		requestDAO.updateSMSSendComplete(param);
	}

	@Override
	public void updateSMSSendInit(SQLParameter param) throws Exception {
		requestDAO.updateSMSSendInit(param);
	}

	@Override
	public int selectLMSReqeustCount(SQLParameter param) throws Exception {
		return requestDAO.selectLMSReqeustCount(param);
	}

	@Override
	public void updateLMSGroupNo(SQLParameter param) throws Exception {
		requestDAO.updateLMSGroupNo(param);
	}

	@Override
	public List<RequestBean> selectLMSRequests(SQLParameter param) throws Exception {
		return requestDAO.selectLMSRequests(param);
	}

	@Override
	public int selectMMSReqeustCount(SQLParameter param) throws Exception {
		return requestDAO.selectMMSReqeustCount(param);
	}

	@Override
	public void updateMMSGroupNo(SQLParameter param) throws Exception {
		requestDAO.updateMMSGroupNo(param);
	}

	@Override
	public List<RequestBean> selectMMSRequests(SQLParameter param) throws Exception {
		return requestDAO.selectMMSRequests(param);
	}

	@Override
	public List<MMSImageBean> selectMMSImage(SQLParameter param) throws Exception {
		return requestDAO.selectMMSImage(param);
	}

	@Override
	public void updateMMSImageGroup(SQLParameter param) throws Exception {
		requestDAO.updateMMSImageGroup(param);
	}

	@Override
	public void Insert_msg_log(Msg_Log _ml) throws Exception {
		requestDAO.Insert_msg_log(_ml);
	}

	@Override
	public int selectMMSImageCount(SQLParameter param) throws Exception {
		return requestDAO.selectMMSImageCount(param);
	}

	@Override
	public LMSTableBean kakao_to_sms_select(Msg_Log ml) throws Exception {
		return requestDAO.kakao_to_sms_select(ml);
	}

	@Override
	public void insert_sms(LMSTableBean lmsBean) throws Exception {
		requestDAO.insert_sms(lmsBean);
	}

}