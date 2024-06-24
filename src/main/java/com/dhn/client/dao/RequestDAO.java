package com.dhn.client.dao;

import java.util.List;

import com.dhn.client.bean.*;

public interface RequestDAO {

	public int selectKAORequestCount(SQLParameter param) throws Exception;

	public void updateKAOGroupNo(SQLParameter param) throws Exception;

	public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception;

	public void updateKAOSendComplete(SQLParameter param) throws Exception;

	public void updateKAOSendInit(SQLParameter param) throws Exception;

	public void Insert_msg_log(Msg_Log _ml) throws Exception;

}
