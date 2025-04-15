package com.dhn.client.service;

import java.util.Collections;
import java.util.List;

import com.dhn.client.bean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dhn.client.dao.RequestDAO;

@Service
public class RequestServiceImpl implements RequestService {

	@Autowired
	private RequestDAO requestDAO;

	@Override
	public int selectMessageRequestCount(SQLParameter param) throws Exception {
		return requestDAO.selectMessageRequestCount(param);
	}

	@Override
	public void updateMessageComplete(SQLParameter param) throws Exception {
		requestDAO.updateMessageComplete(param);
	}

	@Override
	public void updateMessageInit(SQLParameter param) throws Exception {
		requestDAO.updateMessageInit(param);
	}

	@Override
	public void update_msg_log(Msg_Log ml) throws Exception {
		requestDAO.update_msg_log(ml);
	}

	@Override
	public void updateGroupNo(SQLParameter param) throws Exception {
		requestDAO.updateGroupNo(param);
	}

	@Override
	public List<MessageRequestBean> selectKaoMessageRequests(SQLParameter param) throws Exception {
		return requestDAO.selectKaoMessageRequests(param);
	}

	@Override
	public List<MessageRequestBean> selectPushMessageRequests(SQLParameter param) throws Exception {
		return requestDAO.selectPushMessageRequests(param);
	}

	@Override
	public List<MessageRequestBean> selectMsgMessageRequests(SQLParameter param) throws Exception {
		return requestDAO.selectMsgMessageRequests(param);
	}
}
