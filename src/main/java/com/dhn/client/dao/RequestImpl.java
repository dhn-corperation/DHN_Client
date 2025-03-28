package com.dhn.client.dao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dhn.client.bean.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class RequestImpl implements RequestDAO{

	@Autowired
	private SqlSession sqlSession;

	@Override
	public int selectKAORequestCount(SQLParameter param) {
		int cnt = 0;
		cnt = sqlSession.selectOne("com.dhn.client.kakao.mapper.SendRequest.req_kao_count",param);
		return cnt;
	}

	@Override
	public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {
		return sqlSession.selectList("com.dhn.client.kakao.mapper.SendRequest.req_kao_select", param);
	}

	@Override
	public void updateKAOSendComplete(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_sent_complete", param);
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_sent_complete2", param);
	}

	@Override
	public void updateKAOSendInit(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_sent_init", param);
	}

	@Override
	public int selectMSGRequestCount(SQLParameter param) throws Exception {
		int cnt = 0;
		cnt = sqlSession.selectOne("com.dhn.client.msg.mapper.SendRequest.req_msg_count",param);
		return cnt;
	}

	@Override
	public List<RequestBean> selectMSGRequests(SQLParameter param) throws Exception {
		return sqlSession.selectList("com.dhn.client.msg.mapper.SendRequest.req_msg_select", param);
	}

	@Override
	public void updateMSGSendComplete(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.msg.mapper.SendRequest.req_msg_sent_complete",param);
		sqlSession.update("com.dhn.client.msg.mapper.SendRequest.req_msg_sent_complete2",param);
	}

	@Override
	public void updateMSGSendInit(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.msg.mapper.SendRequest.req_msg_sent_init",param);
	}

	@Override
	public void update_msg_log(Msg_Log ml) throws Exception {
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.log_update",ml);
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.dhn_log_update",ml);
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.dhn_log_insert",ml);
		sqlSession.delete("com.dhn.client.result.mapper.SendRequest.dhn_log_delete",ml);
	}

	@Override
	public void logTableCheck(String msgTable, String logTable) throws Exception {
		LocalDate now = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

		String lastMonth = now.minusMonths(1).format(formatter);
		String currentMonth = now.format(formatter);
		String nextMonth = now.plusMonths(1).format(formatter);

		String logTableLast = logTable+"_"+lastMonth;
		String logTableCurrent = logTable+"_"+currentMonth;
		String logTableNext = logTable+"_"+nextMonth;

		Map<String, String> map = new HashMap<>();
		map.put("msgTable", msgTable);

		map.put("logTable",logTableLast);
		int result_last = sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.logTableCheck", map);
		if(result_last == 0){
			sqlSession.update("com.dhn.client.create.mapper.SendRequest.createLogTable", map);
			log.info("{} 테이블 생성",map.get("logTable"));
		}

		map.put("logTable",logTableCurrent);
		int result_current = sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.logTableCheck", map);
		if(result_current == 0){
			sqlSession.update("com.dhn.client.create.mapper.SendRequest.createLogTable", map);
			log.info("{} 테이블 생성",map.get("logTable"));

		}

		map.put("logTable",logTableNext);
		int result_next = sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.logTableCheck", map);
		if(result_next == 0){
			sqlSession.update("com.dhn.client.create.mapper.SendRequest.createLogTable", map);
			log.info("{} 테이블 생성",map.get("logTable"));

		}
	}

	@Override
	public int tableCheck(SQLParameter param) throws Exception {
		return sqlSession.selectOne("com.dhn.client.create.mapper.SendRequest.tableCheck", param);
	}

	@Override
	public void tableCreate(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.create.mapper.SendRequest.createTable", param);
		sqlSession.update("com.dhn.client.create.mapper.SendRequest.createPrimaryKey", param);
		sqlSession.update("com.dhn.client.create.mapper.SendRequest.createIndex1", param);
		sqlSession.update("com.dhn.client.create.mapper.SendRequest.createIndex2", param);
	}

	@Override
	public void phnErrUpdateDelete(Msg_Log ml) throws Exception {
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.phn_err_log_update",ml);
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.phn_err_mst_update",ml);
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.phn_err_dhn_log_update",ml);
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.phn_err_dhn_log_insert",ml);
		sqlSession.delete("com.dhn.client.result.mapper.SendRequest.phn_err_dhn_log_delete",ml);
	}

	@Override
	public void sourceErrUpdate(Msg_Log ml) throws Exception {
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.source_err_log_update",ml);
	}

	@Override
	public void kaoGroupUpdate(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.kao_group_update",param);
	}

	@Override
	public void msgGroupUpdate(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.msg.mapper.SendRequest.msg_group_update",param);
	}

	@Override
	public void update_msg_log_success(Msg_Log result) throws Exception {
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.log_update_suc",result);
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.dhn_log_update_suc",result);
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.dhn_log_insert_suc",result);
		sqlSession.delete("com.dhn.client.result.mapper.SendRequest.dhn_log_delete_suc",result);
	}
}
