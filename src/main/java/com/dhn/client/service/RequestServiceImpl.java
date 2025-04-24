package com.dhn.client.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.dao.RequestDAO; 

@Service
public class RequestServiceImpl implements RequestService{

	@Autowired
	private RequestDAO req;
	
	@Override
	public int selectSMSReqeustCount(SQLParameter param) throws Exception {
		return req.selectSMSReqeustCount(param);
	}

	@Override
	public void updateSMSGroupNo(SQLParameter param) throws Exception {
		req.updateSMSGroupNo(param);
	}
		
	@Override
	public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception {
		return req.selectSMSRequests(param);
	}

	@Override
	public void updateSMSSendComplete(SQLParameter param) throws Exception {
		req.updateSMSSendComplete(param);
	}

	@Override
	public void updateSMSSendInit(SQLParameter param) throws Exception {
		req.updateSMSSendInit(param);
	}

	@Override
	public void Inset_msg_log(Msg_Log ml) throws Exception {
		req.Inset_msg_log(ml);
	}

	@Override
	public int selectMMSReqeustCount(SQLParameter param) throws Exception {
		return req.selectMMSReqeustCount(param);
	}

	@Override
	public void updateMMSGroupNo(SQLParameter param) throws Exception {
		req.updateMMSGroupNo(param);
	}

	@Override
	public List<RequestBean> selectMMSRequests(SQLParameter param) throws Exception {
		return req.selectMMSRequests(param);
	}



}
