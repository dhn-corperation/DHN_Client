package com.dhn.client.service;

import java.util.List;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.MMSImageBean;
import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;

public interface RequestService {

	int selectKAORequestCount(SQLParameter param) throws Exception;

	void updateKAOStatus(SQLParameter param) throws Exception;

	List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception;

	void updateKAOSendComplete(SQLParameter param) throws Exception;

	void updateKAOSendInit(SQLParameter param) throws Exception;
}
