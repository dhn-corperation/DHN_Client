package com.dhn.client.service;

import com.dhn.client.bean.*;

import java.util.List;

public interface RequestService {
	public int selectSMSReqeustCount(SQLParameter param) throws Exception;
	
	public void updateSMSGroupNo(SQLParameter param) throws Exception;

	public void updateSMSSendComplete(SQLParameter param) throws Exception;

	public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception;

	public void updateSMSSendInit(SQLParameter param) throws Exception;

	public void Inset_msg_log(Msg_Log ml) throws Exception;

	public int selectLMSReqeustCount(SQLParameter param) throws Exception;
	
	public void updateLMSGroupNo(SQLParameter param) throws Exception;

	public List<RequestBean> selectLMSRequests(SQLParameter param) throws Exception;

	public int selectMMSReqeustCount(SQLParameter param) throws Exception;

	public void updateMMSGroupNo(SQLParameter param) throws Exception;

	public List<RequestBean> selectMMSRequests(SQLParameter param) throws Exception;

	public List<ImageBean> selectMMSImage(SQLParameter param) throws Exception;

	public int selectOTPReqeustCount(SQLParameter param) throws Exception;
	
	public void updateOTPGroupNo(SQLParameter param) throws Exception;

	public void updateOTPSendComplete(SQLParameter param) throws Exception;

	public List<RequestBean> selectOTPRequests(SQLParameter param) throws Exception;

	public void updateOTPSendInit(SQLParameter param) throws Exception;
	
	public int selectKAOReqeustCount(SQLParameter param) throws Exception;
	
	public void updateKAOGroupNo(SQLParameter param) throws Exception;

	public void updateKAOSendComplete(SQLParameter param) throws Exception;

	public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception;

	public void updateKAOSendInit(SQLParameter param) throws Exception;

	public void checkBackupTable(SQLParameter param) throws Exception;
	
	public void createBackupTable(SQLParameter param) throws Exception;

	public void moveBackupTable(SQLParameter param) throws Exception;

	public void dropBackupTable(SQLParameter param) throws Exception;
	
	public void BT_PH_Replace(SQLParameter param) throws Exception;
	
	public int AliveCount(SQLParameter param) throws Exception;
	
	public void AliveInsert(SQLParameter param) throws Exception;

	public void AliveUpdate(SQLParameter param) throws Exception;

	public int AliveLastCount(SQLParameter param) throws Exception;
	
	public AliveStatusBean getAliveStatus(SQLParameter param) throws Exception;
	
	public void AliveAlarmInsert(SQLParameter param) throws Exception;

}
