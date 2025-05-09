package com.dhn.client.service;

import com.dhn.client.bean.*;
import com.dhn.client.dao.RequestDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
	public void Inset_msg_log(Msg_Log ml) throws Exception {
		req.Inset_msg_log(ml);
	}

	@Override
	public int selectLMSReqeustCount(SQLParameter param) throws Exception {
		return req.selectLMSReqeustCount(param);
	}

	@Override
	public void updateLMSGroupNo(SQLParameter param) throws Exception {
		req.updateLMSGroupNo(param);
	}

	@Override
	public List<RequestBean> selectLMSRequests(SQLParameter param) throws Exception {
		return req.selectLMSRequests(param);
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
	public List<ImageBean> selectMMSImage(SQLParameter param) throws Exception {
		return req.selectMMSImage(param);
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
	public int AliveCount(SQLParameter param) throws Exception {
		return req.AliveCount(param);
	}

	@Override
	public void AliveInsert(SQLParameter param) throws Exception {
		req.AliveInsert(param);
	}

	@Override
	public void AliveUpdate(SQLParameter param) throws Exception {
		req.AliveUpdate(param);
	}

	@Override
	public int AliveLastCount(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		return req.AliveLastCount(param);
	}

	@Override
	public AliveStatusBean getAliveStatus(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		return req.getAliveStatus(param);
	}

	@Override
	public void BT_PH_Replace(SQLParameter param) throws Exception {
		req.BT_PH_Replace(param);
	}

	@Override
	public void AliveAlarmInsert(SQLParameter param) throws Exception {
		req.AliveAlarmInsert(param);
	}


}
