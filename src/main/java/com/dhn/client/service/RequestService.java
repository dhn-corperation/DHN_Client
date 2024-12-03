package com.dhn.client.service;

import java.util.List;

import com.dhn.client.bean.*;

public interface RequestService {

	// 알림톡
	int selectKAORequestCount(SQLParameter param) throws Exception;

	void updateKAOStatus(SQLParameter param) throws Exception;

	List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception;

	void updateKAOSendComplete(SQLParameter param) throws Exception;

	void updateKAOSendInit(SQLParameter param) throws Exception;

	// 푸쉬
	int selectPUSHRequestCount(SQLParameter param) throws Exception;

	void updatePUSHStatus(SQLParameter param) throws Exception;

	List<PUSHRequestBean> selectPUSHRequests(SQLParameter param) throws Exception;

	void updatePUSHSendComplete(SQLParameter param) throws Exception;

	void updatePUSHSendInit(SQLParameter param) throws Exception;

	// 문자
	int selectMSGRequestCount(SQLParameter param) throws Exception;

	void updateMSGStatus(SQLParameter param) throws Exception;

	List<RequestBean> selectMSGRequests(SQLParameter param) throws Exception;

	void updateMSGSendComplete(SQLParameter param) throws Exception;

	void updateMSGSendInit(SQLParameter param) throws Exception;

	void update_msg_log(Msg_Log ml) throws Exception;

}
