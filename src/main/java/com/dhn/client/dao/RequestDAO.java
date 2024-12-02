package com.dhn.client.dao;

import java.util.List;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.PUSHRequestBean;
import com.dhn.client.bean.SQLParameter;

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
}
