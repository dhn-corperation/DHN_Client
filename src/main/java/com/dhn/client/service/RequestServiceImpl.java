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
	public int selectKAORequestCount(SQLParameter param) throws Exception {
		return requestDAO.selectKAORequestCount(param);
	}

	@Override
	public void updateKAOGroupNo(SQLParameter param) throws Exception {
		requestDAO.updateKAOGroupNo(param);
	}

	@Override
	public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {
		return requestDAO.selectKAORequests(param);
	}

	@Override
	public void updateKAOSendComplete(SQLParameter param) throws Exception {
		requestDAO.updateKAOSendComplete(param);
	}

	@Override
	public void updateKAOSendInit(SQLParameter param) throws Exception {
		requestDAO.updateKAOSendInit(param);
	}


	@Override
	public void Insert_msg_log(Msg_Log _ml) throws Exception {
		requestDAO.Insert_msg_log(_ml);
	}

}