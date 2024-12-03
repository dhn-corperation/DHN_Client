package com.dhn.client.dao;

import java.util.List;

import com.dhn.client.bean.*;

public interface RequestDAO {

	int selectKAORequestCount(SQLParameter param) throws Exception;

	void updateKAOStatus(SQLParameter param) throws Exception;

	List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception;

	void updateKAOSendComplete(SQLParameter param) throws Exception;

	void updateKAOSendInit(SQLParameter param) throws Exception;

	int selectPUSHRequestCount(SQLParameter param) throws Exception;

	void updatePUSHStatus(SQLParameter param) throws Exception;

	List<PUSHRequestBean> selectPUSHRequests(SQLParameter param) throws Exception;

	void updatePUSHSendComplete(SQLParameter param) throws Exception;

	void updatePUSHSendInit(SQLParameter param) throws Exception;

    int selectMSGRequestCount(SQLParameter param) throws Exception;

	void updateMSGStatus(SQLParameter param) throws Exception;

	List<RequestBean> selectMSGRequests(SQLParameter param) throws Exception;

	void updateMSGSendComplete(SQLParameter param) throws Exception;

	void updateMSGSendInit(SQLParameter param) throws Exception;

	void update_msg_log(Msg_Log ml) throws Exception;
}
