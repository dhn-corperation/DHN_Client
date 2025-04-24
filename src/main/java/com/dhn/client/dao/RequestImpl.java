package com.dhn.client.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.fasterxml.jackson.annotation.JacksonInject;

@Repository
public class RequestImpl implements RequestDAO{

	@Autowired
	private SqlSession sqlSession;
	 
	@Override
	public int selectSMSReqeustCount(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		int cnt ;

		switch(param.getDBType())
		{
			case "oracle":
				cnt = sqlSession.selectOne("com.dhn.client.oracle.mapper.SendRequest.req_sms_count", param); 
				break;
			case "mysql":			
				cnt = sqlSession.selectOne("com.dhn.client.mysql.mapper.SendRequest.req_sms_count", param); 
				break;
			default:
				cnt = 0;
		}
		
		return cnt;
	}

	@Override
	public void updateSMSGroupNo(SQLParameter param) throws Exception {
		
		switch(param.getDBType())
		{
			case "oracle":
				sqlSession.update("com.dhn.client.oracle.mapper.SendRequest.req_sms_group_update", param);
				break;
			case "mysql":			
				sqlSession.update("com.dhn.client.mysql.mapper.SendRequest.req_sms_group_update", param); 
				break;
		}
	}
	
	@Override
	public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception {

		switch(param.getDBType())
		{
			case "oracle":
				return sqlSession.selectList("com.dhn.client.oracle.mapper.SendRequest.req_sms_select", param);
				
			case "mysql":			
				return sqlSession.selectList("com.dhn.client.mysql.mapper.SendRequest.req_sms_select", param);
				
			default:
				return null;
		}
		
	}

	@Override
	public void updateSMSSendComplete(SQLParameter param) throws Exception {
		switch(param.getDBType())
		{
			case "oracle":
				sqlSession.update("com.dhn.client.oracle.mapper.SendRequest.req_sent_complete", param);
				break;
			case "mysql":			
				sqlSession.update("com.dhn.client.mysql.mapper.SendRequest.req_sent_complete", param); 
				break;
		}
		
	}

	@Override
	public void updateSMSSendInit(SQLParameter param) throws Exception {
		switch(param.getDBType())
		{
			case "oracle":
				sqlSession.update("com.dhn.client.oracle.mapper.SendRequest.req_sent_init", param);
				break;
			case "mysql":			
				sqlSession.update("com.dhn.client.mysql.mapper.SendRequest.req_sent_init", param); 
				break;
		}
	}

	@Override
	public void Inset_msg_log(Msg_Log ml) throws Exception {
		switch(ml.getDBType())
		{
			case "oracle":
				try {
					sqlSession.insert("com.dhn.client.oracle.mapper.SendRequest.result_log_insert1", ml);
					sqlSession.delete("com.dhn.client.oracle.mapper.SendRequest.result_log_insert2", ml);
					sqlSession.update("com.dhn.client.oracle.mapper.SendRequest.result_log_insert3", ml);
				} catch(Exception ex) {
					if(ex.getMessage().contains("ORA-00942"))
					{
						sqlSession.update("com.dhn.client.oracle.mapper.SendRequest.create_log_table", ml);
						sqlSession.insert("com.dhn.client.oracle.mapper.SendRequest.result_log_insert1", ml);
						sqlSession.delete("com.dhn.client.oracle.mapper.SendRequest.result_log_insert2", ml);
						sqlSession.update("com.dhn.client.oracle.mapper.SendRequest.result_log_insert3", ml);
					}
				}

				break;
			case "mysql":			
				try {
					sqlSession.insert("com.dhn.client.mysql.mapper.SendRequest.result_log_insert1", ml);
					sqlSession.delete("com.dhn.client.mysql.mapper.SendRequest.result_log_insert2", ml);
					sqlSession.update("com.dhn.client.mysql.mapper.SendRequest.result_log_insert3", ml);
				}catch(Exception ex) {
					//System.out.println(ex.getMessage());
					if(ex.getMessage().contains("doesn't exist"))
					{
						//System.out.println("Create Table : " + ml.getMsg_log());
						sqlSession.update("com.dhn.client.mysql.mapper.SendRequest.create_log_table", ml);
						sqlSession.insert("com.dhn.client.mysql.mapper.SendRequest.result_log_insert1", ml);
						sqlSession.delete("com.dhn.client.mysql.mapper.SendRequest.result_log_insert2", ml);
						sqlSession.update("com.dhn.client.mysql.mapper.SendRequest.result_log_insert3", ml);
					}
				}
				break;
		}
	}

	@Override
	public int selectMMSReqeustCount(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		int cnt ;

		switch(param.getDBType())
		{
			case "oracle":
				cnt = sqlSession.selectOne("com.dhn.client.oracle.mapper.SendRequest.req_mms_count", param); 
				break;
			case "mysql":			
				cnt = sqlSession.selectOne("com.dhn.client.mysql.mapper.SendRequest.req_mms_count", param); 
				break;
			default:
				cnt = 0;
		}
		
		return cnt;
	}

	@Override
	public void updateMMSGroupNo(SQLParameter param) throws Exception {
		switch(param.getDBType())
		{
			case "oracle":
				sqlSession.update("com.dhn.client.oracle.mapper.SendRequest.req_mms_group_update", param);
				break;
			case "mysql":			
				sqlSession.update("com.dhn.client.mysql.mapper.SendRequest.req_mms_group_update", param); 
				break;
		}
	}

	@Override
	public List<RequestBean> selectMMSRequests(SQLParameter param) throws Exception {
		switch(param.getDBType())
		{
			case "oracle":
				return sqlSession.selectList("com.dhn.client.oracle.mapper.SendRequest.req_mms_select", param);
				
			case "mysql":			
				return sqlSession.selectList("com.dhn.client.mysql.mapper.SendRequest.req_mms_select", param);
				
			default:
				return null;
		}
	}
 
}
