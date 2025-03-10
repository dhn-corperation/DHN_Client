package com.dhn.client.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.KAOtoMMSBean;
import com.dhn.client.bean.LMSTableBean;
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
		int cnt =0 ;
		if(param.getZbsysmcd_table() != null && param.getZbsysmcd_table().length()>0) {
			cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_sms_count_z", param); 
		} else {
			cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_sms_count", param); 
		}
		return cnt;
	}

	@Override
	public void updateSMSGroupNo(SQLParameter param) throws Exception {
		
		if(param.getZbsysmcd_table() != null && param.getZbsysmcd_table().length()>0) {
			sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_sms_group_update_z", param);
		} else {
			sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_sms_group_update", param);
		}
	}
	
	@Override
	public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception {

		return sqlSession.selectList("com.dhn.client.nkakao.mapper.SendRequest.req_sms_select", param);
		
	}

	@Override
	public void updateSMSSendComplete(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_sent_complete", param); 
	}

	@Override
	public void updateSMSSendInit(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_sent_init", param); 
	}

	@Override
	public void Insert_msg_log(Msg_Log ml) throws Exception {
		
		
		if(ml.getMsg_type().equals("K")) {
			sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert1", ml);
			sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert2", ml);
			sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert3", ml);
		} else {
			sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.result_log_insert1", ml);
			if(ml.getSecond_flag().equalsIgnoreCase("N")) {
				sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.result_log_insert2", ml);
			} else {
				sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.result_log_insert3", ml);
			}
			sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.result_log_insert4", ml);
		}
		
	}

	@Override
	public int selectMMSReqeustCount(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		int cnt ;

		if(param.getZbsysmcd_table() != null && param.getZbsysmcd_table().length()>0) {
			cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_mms_count_z", param);
		} else {
			cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_mms_count", param);
		}

		return cnt;
	}

	@Override
	public void updateMMSGroupNo(SQLParameter param) throws Exception {
		if(param.getZbsysmcd_table() != null && param.getZbsysmcd_table().length()>0) {
			sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_mms_group_update_z", param);
		} else {
			sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_mms_group_update", param);
		}
	}

	@Override
	public List<RequestBean> selectMMSRequests(SQLParameter param) throws Exception {
		return sqlSession.selectList("com.dhn.client.nkakao.mapper.SendRequest.req_mms_select", param);
	}
 
	 
	@Override
	public int selectOTPReqeustCount(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		int cnt =0 ;

		cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_otp_count", param); 
		
		return cnt;
	}

	@Override
	public void updateOTPGroupNo(SQLParameter param) throws Exception {
		
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_otp_group_update", param); 
	}
	
	@Override
	public List<RequestBean> selectOTPRequests(SQLParameter param) throws Exception {

		return sqlSession.selectList("com.dhn.client.nkakao.mapper.SendRequest.req_otp_select", param);
		
	}

	@Override
	public void updateOTPSendComplete(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_otp_sent_complete", param); 
	}

	@Override
	public void updateOTPSendInit(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_otp_sent_init", param); 
	}
	
	 
	@Override
	public int selectKAOReqeustCount(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		int cnt =0 ;

		if(param.getZbsysmcd_table() != null && param.getZbsysmcd_table().length()>0) {
			cnt = sqlSession.selectOne("com.dhn.client.kakao.mapper.SendRequest.req_kao_count_z", param);
		} else {
			cnt = sqlSession.selectOne("com.dhn.client.kakao.mapper.SendRequest.req_kao_count", param);
		}
		
		return cnt;
	}

	@Override
	public void updateKAOGroupNo(SQLParameter param) throws Exception {
		
		if(param.getZbsysmcd_table() != null && param.getZbsysmcd_table().length()>0) {
			sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_kao_group_update_z", param);
		} else {
			sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_kao_group_update", param);
		}
	}
	
	@Override
	public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {

		return sqlSession.selectList("com.dhn.client.kakao.mapper.SendRequest.req_kao_select", param);
		
	}

	@Override
	public void updateKAOSendComplete(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_sent_complete", param); 
	}

	@Override
	public void updateKAOSendInit(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_sent_init", param); 
	}

	@Override
	public List<LMSTableBean> kakao_to_sms_select(Msg_Log param) throws Exception {
		return sqlSession.selectList("com.dhn.client.nkakao.mapper.SendRequest.kakao_to_sms_select", param); 
	}

	@Override
	public void insert_sms(LMSTableBean param) throws Exception {
		sqlSession.insert("com.dhn.client.nkakao.mapper.SendRequest.kakao_to_sms_insert", param); 
	}

	@Override
	public int selectOldDataCount(SQLParameter param) throws Exception {
		int cnt =0;

		if(param.getZbsysmcd_table() != null && param.getZbsysmcd_table().length()>0) {
			cnt = sqlSession.selectOne("com.dhn.client.result.mapper.SendRequest.old_data_count_z", param);
		} else {
			cnt = sqlSession.selectOne("com.dhn.client.result.mapper.SendRequest.old_data_count", param);
		}

		return cnt;
	}

	@Override
	public List<RequestBean> selectOldDataId(SQLParameter param) throws Exception {
		if(param.getZbsysmcd_table() != null && param.getZbsysmcd_table().length()>0) {
			return sqlSession.selectList("com.dhn.client.result.mapper.SendRequest.old_data_list_z", param);
		} else {
			return sqlSession.selectList("com.dhn.client.result.mapper.SendRequest.old_data_list", param);
		}
	}

	@Override
	public void oldDataResult(Msg_Log ml) throws Exception {
		sqlSession.update("com.dhn.client.result.mapper.SendRequest.old_data_update", ml);
		sqlSession.insert("com.dhn.client.result.mapper.SendRequest.old_data_insert", ml);
		sqlSession.delete("com.dhn.client.result.mapper.SendRequest.old_data_delete", ml);
	}

}
