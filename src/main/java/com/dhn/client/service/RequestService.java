package com.dhn.client.service;

import java.util.HashMap;
import java.util.List;

import com.dhn.client.bean.*;

public interface RequestService {
	public int selectSMSReqeustCount(SQLParameter param) throws Exception;
	
	public void updateSMSGroupNo(SQLParameter param) throws Exception;

	public void updateSMSSendComplete(SQLParameter param) throws Exception;

	public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception;

	public void updateSMSSendInit(SQLParameter param) throws Exception;

	public void Insert_msg_log(Msg_Log ml) throws Exception;

	public int selectMMSReqeustCount(SQLParameter param) throws Exception;
	
	public void updateMMSGroupNo(SQLParameter param) throws Exception;

	public List<RequestBean> selectMMSRequests(SQLParameter param) throws Exception;

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

	public List<LMSTableBean> kakao_to_sms_select(Msg_Log param) throws Exception;
	
	public void insert_sms(LMSTableBean param) throws Exception;

    public int selectOldDataCount(SQLParameter param) throws Exception;

	public List<OldResultBean> selectOldData(SQLParameter param) throws Exception;

	public void oldDataResult(Msg_Log ml) throws Exception;
}
