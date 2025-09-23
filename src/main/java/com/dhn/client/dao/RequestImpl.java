package com.dhn.client.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	
	private static final Logger log = LogManager.getRootLogger();
	 
	@Override
	public int selectSMSReqeustCount(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		int cnt =0 ;

		cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_sms_count", param); 
		
		return cnt;
	}

	@Override
	public void updateSMSGroupNo(SQLParameter param) throws Exception {
		
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_sms_group_update", param); 
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
		
		try {
			sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert1", ml);
			sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert2", ml);
			sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert3", ml);
		} catch (Exception ex) {
			//log.info("에러", ex.getMessage(), ex.getMessage().indexOf(ml.getLog_table()));
			if(ex.getMessage().indexOf(ml.getLog_table())>0) {
				sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.create_log_table", ml);
				sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert1", ml);
				sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert2", ml);
				sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert3", ml);
			}
		}
	}

	@Override
	public int selectMMSReqeustCount(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		int cnt ;

		cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_mms_count", param); 

		return cnt;
	}

	@Override
	public void updateMMSGroupNo(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_mms_group_update", param); 
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

		cnt = sqlSession.selectOne("com.dhn.client.kakao.mapper.SendRequest.req_kao_count", param); 
		
		return cnt;
	}

	@Override
	public void updateKAOGroupNo(SQLParameter param) throws Exception {
		
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_kao_group_update", param); 
	}
	
	@Override
	public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {
		if(param.getKakaobtn() != null && param.getKakaobtn().equals("Y")) {
			return sqlSession.selectList("com.dhn.client.kakaobtn.mapper.SendRequest.req_kao_select", param);
		} else {
			return sqlSession.selectList("com.dhn.client.kakao.mapper.SendRequest.req_kao_select", param);
		}
		
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
	public void checkBackupTable(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_table_backup_1", param);
	}

	@Override
	public void createBackupTable(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_table_backup_2", param);
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_table_backup_3", param);
	}

	@Override
	public void moveBackupTable(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_table_backup_4", param);
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_table_backup_5", param);
	}

	@Override
	public void dropBackupTable(SQLParameter param) throws Exception {
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.drop_backup_table", param);
	}

	@Override
	public void Insert_sms(LMSTableBean lmst) throws Exception {
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.Insert_sms", lmst);
	}

	@Override
	public void Insert_lms(LMSTableBean lmst) throws Exception {
		sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.Insert_lms", lmst);
	}

	@Override
	public int kakao_to_sms_count(KAOtoMMSBean param) throws Exception {
		int cnt =0 ;

		cnt = sqlSession.selectOne("com.dhn.client.kakao.mapper.SendRequest.kao_to_sms_count", param); 
		
		return cnt;
	}

	@Override
	public void kakao_to_sms_group_update(KAOtoMMSBean param) throws Exception {
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.kao_to_sms_group_update", param);
	}

	@Override
	public void kakao_to_sms_move(KAOtoMMSBean param) throws Exception {
		sqlSession.insert("com.dhn.client.kakao.mapper.SendRequest.kao_to_sms_copy", param);
		
		sqlSession.insert("com.dhn.client.kakao.mapper.SendRequest.kao_to_sms_delete", param);
	}

	@Override
	public int kakao_to_mms_count(KAOtoMMSBean param) throws Exception {
		int cnt =0 ;

		cnt = sqlSession.selectOne("com.dhn.client.kakao.mapper.SendRequest.kao_to_mms_count", param); 
		
		return cnt;
	}

	@Override
	public void kakao_to_mms_group_update(KAOtoMMSBean param) throws Exception {
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.kao_to_mms_group_update", param);
	}

	@Override
	public void kakao_to_mms_move(KAOtoMMSBean param) throws Exception {
		sqlSession.insert("com.dhn.client.kakao.mapper.SendRequest.kao_to_mms_copy", param);

		sqlSession.insert("com.dhn.client.kakao.mapper.SendRequest.kao_to_mms_delete", param);
	}

	@Override
	public int kaox_to_sms_count(KAOtoMMSBean param) throws Exception {
		int cnt = 0;
		
		cnt = sqlSession.selectOne("com.dhn.client.kakao.mapper.SendRequest.kaox_to_sms_count", param);
		
		return cnt;
	}

	@Override
	public void kaox_to_tran_type_update(KAOtoMMSBean param) throws Exception {
		sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.kaox_to_tran_type_update", param);
	}
		
}
