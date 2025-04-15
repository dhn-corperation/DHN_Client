package com.dhn.client.dao;

import java.util.Collections;
import java.util.List;

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
	public int selectMessageRequestCount(SQLParameter param) throws Exception {
		int cnt = 0;
		cnt = sqlSession.selectOne("com.dhn.client.message.mapper.SendRequest.req_message_count",param);
		return cnt;
	}

	@Override
	public void updateMessageComplete(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.message.mapper.SendRequest.req_message_sent_complete",param);
	}

	@Override
	public void updateMessageInit(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.message.mapper.SendRequest.req_message_sent_init",param);
	}

	@Override
	public void update_msg_log(Msg_Log ml) throws Exception {
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.log_update",ml);
	}

	@Override
	public void updateGroupNo(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.message.mapper.SendRequest.update_group_no",param);
	}

	@Override
	public List<MessageRequestBean> selectKaoMessageRequests(SQLParameter param) throws Exception {
		return sqlSession.selectList("com.dhn.client.message.mapper.SendRequest.req_kao_message_select", param);
	}

	@Override
	public List<MessageRequestBean> selectPushMessageRequests(SQLParameter param) throws Exception {
		return sqlSession.selectList("com.dhn.client.message.mapper.SendRequest.req_push_message_select", param);
	}

	@Override
	public List<MessageRequestBean> selectMsgMessageRequests(SQLParameter param) throws Exception {
		return sqlSession.selectList("com.dhn.client.message.mapper.SendRequest.req_msg_message_select", param);
	}
}
