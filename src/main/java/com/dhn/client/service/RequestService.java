package com.dhn.client.service;

import java.util.List;

import com.dhn.client.bean.*;

public interface RequestService {

	// 모든발송
	int selectMessageRequestCount(SQLParameter param) throws Exception;

	List<MessageRequestBean> selectMessageRequests(SQLParameter param) throws Exception;

	void updateMessageComplete(SQLParameter param) throws Exception;

	void updateMessageInit(SQLParameter param) throws Exception;

	// 결과 처리
	void update_msg_log(Msg_Log ml) throws Exception;

	void updateMessageStatus(SQLParameter param) throws Exception;
}
