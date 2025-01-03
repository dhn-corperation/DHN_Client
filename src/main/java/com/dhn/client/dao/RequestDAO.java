package com.dhn.client.dao;

import java.util.List;

import com.dhn.client.bean.*;

public interface RequestDAO {

	int selectMessageRequestCount(SQLParameter param) throws Exception;

	List<MessageRequestBean> selectMessageRequests(SQLParameter param) throws Exception;

	void updateMessageComplete(SQLParameter param) throws Exception;

	void updateMessageInit(SQLParameter param) throws Exception;

	void update_msg_log(Msg_Log ml) throws Exception;

}
