package com.dhn.client.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.KAOtoMMSBean;
import com.dhn.client.bean.LMSTableBean;
import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.dao.RequestDAO; 

@Service
public class RequestServiceImpl implements RequestService{

	@Autowired
	private RequestDAO requestDAO;
	
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
	public void Insert_msg_log(Msg_Log ml) throws Exception {
		requestDAO.Insert_msg_log(ml);
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
	public int selectOTPReqeustCount(SQLParameter param) throws Exception {
		return requestDAO.selectOTPReqeustCount(param);
	}

	@Override
	public void updateOTPGroupNo(SQLParameter param) throws Exception {
		requestDAO.updateOTPGroupNo(param);
	}
		
	@Override
	public List<RequestBean> selectOTPRequests(SQLParameter param) throws Exception {
		return requestDAO.selectOTPRequests(param);
	}

	@Override
	public void updateOTPSendComplete(SQLParameter param) throws Exception {
		requestDAO.updateOTPSendComplete(param);
	}

	@Override
	public void updateOTPSendInit(SQLParameter param) throws Exception {
		requestDAO.updateOTPSendInit(param);
	}


	@Override
	public int selectKAOReqeustCount(SQLParameter param) throws Exception {
		return requestDAO.selectKAOReqeustCount(param);
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
	public List<LMSTableBean> kakao_to_sms_select(Msg_Log param) throws Exception {
		return requestDAO.kakao_to_sms_select(param);
	}

	@Override
	public void insert_sms(LMSTableBean param) throws Exception {
		requestDAO.insert_sms(param);
	}
 

}
