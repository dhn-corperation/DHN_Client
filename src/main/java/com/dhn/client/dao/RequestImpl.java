package com.dhn.client.dao;

import java.util.Collections;
import java.util.List;

import com.dhn.client.bean.*;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
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
	public void updateKAOStatus(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_kao_status_update",param);
	}

	@Override
	public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {
		return sqlSession.selectList("com.dhn.client.kakao.mapper.SendRequest.req_kao_select", param);
	}

	@Override
	public void updateKAOSendComplete(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_sent_complet", param);
	}

	@Override
	public void updateKAOSendInit(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_sent_init", param);
	}

	@Override
	public int selectPUSHRequestCount(SQLParameter param) throws Exception {
		int cnt = 0;
		cnt = sqlSession.selectOne("com.dhn.client.push.mapper.SendRequest.req_push_count",param);
		return cnt;
	}

	@Override
	public void updatePUSHStatus(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.push.mapper.SendRequest.req_push_status_update",param);
	}

	@Override
	public List<PUSHRequestBean> selectPUSHRequests(SQLParameter param) throws Exception {
		return sqlSession.selectList("com.dhn.client.push.mapper.SendRequest.req_push_select", param);
	}

	@Override
	public void updatePUSHSendComplete(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.push.mapper.SendRequest.req_push_sent_complet",param);
	}

	@Override
	public void updatePUSHSendInit(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.push.mapper.SendRequest.req_push_sent_init",param);
	}
}
