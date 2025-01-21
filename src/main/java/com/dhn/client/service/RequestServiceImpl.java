package com.dhn.client.service;

import java.util.Collections;
import java.util.List;

import com.dhn.client.bean.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dhn.client.dao.RequestDAO;

@Service
@Slf4j
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
	public int selectMSGRequestCount(SQLParameter param) throws Exception {
		return requestDAO.selectMSGRequestCount(param);
	}

	@Override
	public void updateMSGStatus(SQLParameter param) throws Exception {
		requestDAO.updateMSGStatus(param);
	}

	@Override
	public List<RequestBean> selectMSGRequests(SQLParameter param) throws Exception {
		return requestDAO.selectMSGRequests(param);
	}

	@Override
	public void updateMSGSendComplete(SQLParameter param) throws Exception {
		requestDAO.updateMSGSendComplete(param);
	}

	@Override
	public void updateMSGSendInit(SQLParameter param) throws Exception {
		requestDAO.updateMSGSendInit(param);
	}

	@Override
	public void update_msg_log(Msg_Log ml) throws Exception {
		requestDAO.update_msg_log(ml);
	}

	@Override
	public void logTableCheck(String msg_Table, String log_Table) throws Exception {
		requestDAO.logTableCheck(msg_Table, log_Table);
	}

	@Override
	public void tableCheck(SQLParameter param) throws Exception {
		int result = requestDAO.tableCheck(param);

		try{
			if(result == 0){
				requestDAO.tableCreate(param);
				log.info("{} 테이블 생성 완료",param.getMsg_table());
			}else{
				log.info("{} 테이블이 존재합니다.",param.getMsg_table());
			}
		}catch (Exception e){
			log.error("{} 테이블 생성 중 오류 발생: {}", param.getMsg_table(), e.getMessage());
			throw e;
		}
		requestDAO.tableCheck(param);
	}
}
