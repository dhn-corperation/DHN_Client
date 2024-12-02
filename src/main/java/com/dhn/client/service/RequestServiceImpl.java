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
	public void updateKAOStatus(SQLParameter param) throws Exception {
		requestDAO.updateKAOStatus(param);
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
	public int selectPUSHRequestCount(SQLParameter param) throws Exception {
		return requestDAO.selectPUSHRequestCount(param);
	}

	@Override
	public void updatePUSHStatus(SQLParameter param) throws Exception {
		requestDAO.updatePUSHStatus(param);
	}

	@Override
	public List<PUSHRequestBean> selectPUSHRequests(SQLParameter param) throws Exception {
		return requestDAO.selectPUSHRequests(param);
	}

	@Override
	public void updatePUSHSendComplete(SQLParameter param) throws Exception {
		requestDAO.updatePUSHSendComplete(param);
	}

	@Override
	public void updatePUSHSendInit(SQLParameter param) throws Exception {
		requestDAO.updatePUSHSendInit(param);
	}
}