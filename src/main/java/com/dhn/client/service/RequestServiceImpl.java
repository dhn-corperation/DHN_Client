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
	private RequestDAO req;
	
	@Override
	public int selectSMSReqeustCount(SQLParameter param) throws Exception {
		return req.selectSMSReqeustCount(param);
	}

	@Override
	public void updateSMSGroupNo(SQLParameter param) throws Exception {
		req.updateSMSGroupNo(param);
	}
		
	@Override
	public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception {
		return req.selectSMSRequests(param);
	}

	@Override
	public void updateSMSSendComplete(SQLParameter param) throws Exception {
		req.updateSMSSendComplete(param);
	}

	@Override
	public void updateSMSSendInit(SQLParameter param) throws Exception {
		req.updateSMSSendInit(param);
	}

	@Override
	public void Insert_msg_log(Msg_Log ml) throws Exception {
		req.Insert_msg_log(ml);
	}

	@Override
	public int selectMMSReqeustCount(SQLParameter param) throws Exception {
		return req.selectMMSReqeustCount(param);
	}

	@Override
	public void updateMMSGroupNo(SQLParameter param) throws Exception {
		req.updateMMSGroupNo(param);
	}

	@Override
	public List<RequestBean> selectMMSRequests(SQLParameter param) throws Exception {
		return req.selectMMSRequests(param);
	}


	@Override
	public int selectOTPReqeustCount(SQLParameter param) throws Exception {
		return req.selectOTPReqeustCount(param);
	}

	@Override
	public void updateOTPGroupNo(SQLParameter param) throws Exception {
		req.updateOTPGroupNo(param);
	}
		
	@Override
	public List<RequestBean> selectOTPRequests(SQLParameter param) throws Exception {
		return req.selectOTPRequests(param);
	}

	@Override
	public void updateOTPSendComplete(SQLParameter param) throws Exception {
		req.updateOTPSendComplete(param);
	}

	@Override
	public void updateOTPSendInit(SQLParameter param) throws Exception {
		req.updateOTPSendInit(param);
	}


	@Override
	public int selectKAOReqeustCount(SQLParameter param) throws Exception {
		return req.selectKAOReqeustCount(param);
	}

	@Override
	public void updateKAOGroupNo(SQLParameter param) throws Exception {
		req.updateKAOGroupNo(param);
	}
		
	@Override
	public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {
		return req.selectKAORequests(param);
	}

	@Override
	public void updateKAOSendComplete(SQLParameter param) throws Exception {
		req.updateKAOSendComplete(param);
	}

	@Override
	public void updateKAOSendInit(SQLParameter param) throws Exception {
		req.updateKAOSendInit(param);
	}

	@Override
	public void checkBackupTable(SQLParameter param) throws Exception {
		req.checkBackupTable(param);
	}

	@Override
	public void createBackupTable(SQLParameter param) throws Exception {
		req.createBackupTable(param);
	}

	@Override
	public void moveBackupTable(SQLParameter param) throws Exception {
		req.moveBackupTable(param);
	}

	@Override
	public void dropBackupTable(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		req.dropBackupTable(param);
	}

	@Override
	public void Insert_sms(LMSTableBean lmst) throws Exception {
		req.Insert_sms(lmst);
	}

	@Override
	public void Insert_lms(LMSTableBean lmst) throws Exception {
		req.Insert_lms(lmst);
	}

	@Override
	public int kakao_to_sms_count(KAOtoMMSBean param) throws Exception {
		return req.kakao_to_sms_count(param);
	}

	@Override
	public void kakao_to_sms_group_update(KAOtoMMSBean param) throws Exception {
		req.kakao_to_sms_group_update(param);
	}

	@Override
	public void kakao_to_sms_move(KAOtoMMSBean param) throws Exception {
		req.kakao_to_sms_move(param);
	}

	@Override
	public int kakao_to_mms_count(KAOtoMMSBean param) throws Exception {
		return req.kakao_to_mms_count(param);
	}

	@Override
	public void kakao_to_mms_group_update(KAOtoMMSBean param) throws Exception {
		req.kakao_to_mms_group_update(param);
	}

	@Override
	public void kakao_to_mms_move(KAOtoMMSBean param) throws Exception {
		req.kakao_to_mms_move(param);
	}

	@Override
	public int kaox_to_sms_count(KAOtoMMSBean param) throws Exception {
		 
		return req.kaox_to_sms_count(param);
	}

	@Override
	public void kaox_to_tran_type_update(KAOtoMMSBean param) throws Exception {
		req.kaox_to_tran_type_update(param);
	}


}
