package com.dhn.client.dao;

import com.dhn.client.bean.*;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RequestImpl implements RequestDAO{

	@Autowired
	private SqlSession sqlSession;
	 
	@Override
	public int selectSMSReqeustCount(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		int cnt =0 ;
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			if(param.isOtpFlag()) {
				cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_sms_count", param);
			} else {
				cnt = sqlSession.selectOne("com.dhn.client.notp.mapper.postgresql.SendRequest.req_sms_count", param);
			}
		} else {
			if(param.isOtpFlag()) {
				cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_sms_count", param);
			} else {
				cnt = sqlSession.selectOne("com.dhn.client.notp.mapper.SendRequest.req_sms_count", param);
			}
		}
		return cnt;
	}

	@Override
	public void updateSMSGroupNo(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			if(param.isOtpFlag()) {
				sqlSession.update("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_sms_group_update", param);
			} else {
				sqlSession.update("com.dhn.client.notp.mapper.postgresql.SendRequest.req_sms_group_update", param);
			}
		} else {
			if(param.isOtpFlag()) {
				sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_sms_group_update", param);
			} else {
				sqlSession.update("com.dhn.client.notp.mapper.SendRequest.req_sms_group_update", param);
			}
		}
	}
	
	@Override
	public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception {

		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			if(param.isOtpFlag()) {
				return sqlSession.selectList("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_sms_select", param);
			} else {
				return sqlSession.selectList("com.dhn.client.notp.mapper.postgresql.SendRequest.req_sms_select", param);
			}
		} else {
			if(param.isOtpFlag()) {
				return sqlSession.selectList("com.dhn.client.nkakao.mapper.SendRequest.req_sms_select", param);
			} else {
				return sqlSession.selectList("com.dhn.client.notp.mapper.SendRequest.req_sms_select", param);
			}
		}
		
	}

	@Override
	public void updateSMSSendComplete(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.update("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_sent_complete", param); 
		} else {
			sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_sent_complete", param); 
		}
	}

	@Override
	public void updateSMSSendInit(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.update("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_sent_init", param); 
		} else {
			sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_sent_init", param); 
		}
	}

	@Override
	public void Inset_msg_log(Msg_Log ml) throws Exception {
		if(ml.getDatabase() != null && ml.getDatabase().toLowerCase().equals("postgresql"))
		{
			if(ml.isKakao()) {
				sqlSession.update("com.dhn.client.kakao.mapper.postgresql.SendRequest.result_log_insert3", ml);
			} else {
				sqlSession.update("com.dhn.client.nkakao.mapper.postgresql.SendRequest.result_log_insert3", ml);
			}
		} else {
			if(ml.isKakao()) {
				sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.result_log_insert3", ml);
			} else {
				sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.result_log_insert3", ml);
			}
		}
	}

	@Override
	public int selectLMSReqeustCount(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		int cnt ;
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			if(param.isOtpFlag()) {
				cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_lms_count", param);
			} else {
				cnt = sqlSession.selectOne("com.dhn.client.notp.mapper.postgresql.SendRequest.req_lms_count", param);
			}
		} else {
			if(param.isOtpFlag()) {
				cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_lms_count", param);
			} else {
				cnt = sqlSession.selectOne("com.dhn.client.notp.mapper.SendRequest.req_lms_count", param);
			}
		}

		return cnt;
	}

	@Override
	public void updateLMSGroupNo(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			if(param.isOtpFlag()) {
				sqlSession.update("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_lms_group_update", param);
			} else {
				sqlSession.update("com.dhn.client.notp.mapper.postgresql.SendRequest.req_lms_group_update", param);
			}
		} else {
			if(param.isOtpFlag()) {
				sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_lms_group_update", param);
			} else {
				sqlSession.update("com.dhn.client.notp.mapper.SendRequest.req_lms_group_update", param);
			}
		}
	}

	@Override
	public List<RequestBean> selectLMSRequests(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			if(param.isOtpFlag()) {
				return sqlSession.selectList("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_lms_select", param);
			} else {
				return sqlSession.selectList("com.dhn.client.notp.mapper.postgresql.SendRequest.req_lms_select", param);
			}
		} else {
			if(param.isOtpFlag()) {
				return sqlSession.selectList("com.dhn.client.nkakao.mapper.SendRequest.req_lms_select", param);
			} else {
				return sqlSession.selectList("com.dhn.client.notp.mapper.SendRequest.req_lms_select", param);
			}
		}
	}

	@Override
	public int selectMMSReqeustCount(SQLParameter param) throws Exception {
		int cnt ;
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			if(param.isOtpFlag()) {
				cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_mms_count", param);
			} else {
				cnt = sqlSession.selectOne("com.dhn.client.notp.mapper.postgresql.SendRequest.req_mms_count", param);
			}
		} else {
			if(param.isOtpFlag()) {
				cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_mms_count", param);
			} else {
				cnt = sqlSession.selectOne("com.dhn.client.notp.mapper.SendRequest.req_mms_count", param);
			}
		}

		return cnt;
	}

	@Override
	public void updateMMSGroupNo(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			if(param.isOtpFlag()) {
				sqlSession.update("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_mms_group_update", param);
			} else {
				sqlSession.update("com.dhn.client.notp.mapper.postgresql.SendRequest.req_mms_group_update", param);
			}
		} else {
			if(param.isOtpFlag()) {
				sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_mms_group_update", param);
			} else {
				sqlSession.update("com.dhn.client.notp.mapper.SendRequest.req_mms_group_update", param);
			}
		}
	}

	@Override
	public List<RequestBean> selectMMSRequests(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			if(param.isOtpFlag()) {
				return sqlSession.selectList("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_mms_select", param);
			} else {
				return sqlSession.selectList("com.dhn.client.notp.mapper.postgresql.SendRequest.req_mms_select", param);
			}
		} else {
			if(param.isOtpFlag()) {
				return sqlSession.selectList("com.dhn.client.nkakao.mapper.SendRequest.req_mms_select", param);
			} else {
				return sqlSession.selectList("com.dhn.client.notp.mapper.SendRequest.req_mms_select", param);
			}
		}
	}

	@Override
	public List<ImageBean> selectMMSImage(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			return sqlSession.selectList("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_img_select", param);
		} else {
			return sqlSession.selectList("com.dhn.client.nkakao.mapper.SendRequest.req_img_select", param);
		}
	}


	@Override
	public int selectOTPReqeustCount(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		int cnt =0 ;

		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_otp_count", param); 
		} else {
			cnt = sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_otp_count", param);
		} 
		
		return cnt;
	}

	@Override
	public void updateOTPGroupNo(SQLParameter param) throws Exception {
		
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.update("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_otp_group_update", param); 
		} else {
			sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_otp_group_update", param);
		}
	}
	
	@Override
	public List<RequestBean> selectOTPRequests(SQLParameter param) throws Exception {

		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			return sqlSession.selectList("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_otp_select", param);
		} else {
			return sqlSession.selectList("com.dhn.client.nkakao.mapper.SendRequest.req_otp_select", param);
		}
		
	}

	@Override
	public void updateOTPSendComplete(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.update("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_otp_sent_complete", param); 
		} else {
			sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_otp_sent_complete", param); 
		}
	}

	@Override
	public void updateOTPSendInit(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.update("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_otp_sent_init", param); 
		} else {
			sqlSession.update("com.dhn.client.nkakao.mapper.SendRequest.req_otp_sent_init", param); 
		}
	}
	
	 
	@Override
	public int selectKAOReqeustCount(SQLParameter param) throws Exception {
		// TODO Auto-generated method stub
		int cnt =0 ;

		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			cnt = sqlSession.selectOne("com.dhn.client.kakao.mapper.postgresql.SendRequest.req_kao_count", param); 
		} else {
			cnt = sqlSession.selectOne("com.dhn.client.kakao.mapper.SendRequest.req_kao_count", param); 
		}
		
		return cnt;
	}

	@Override
	public void updateKAOGroupNo(SQLParameter param) throws Exception {
		
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.update("com.dhn.client.kakao.mapper.postgresql.SendRequest.req_kao_group_update", param); 
		} else {
			sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_kao_group_update", param); 
		} 
	}
	
	@Override
	public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {

		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			return sqlSession.selectList("com.dhn.client.kakao.mapper.postgresql.SendRequest.req_kao_select", param);
		} else {
			return sqlSession.selectList("com.dhn.client.kakao.mapper.SendRequest.req_kao_select", param);
		}
		
	}

	@Override
	public void updateKAOSendComplete(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.update("com.dhn.client.kakao.mapper.postgresql.SendRequest.req_sent_complete", param); 
		} else {
			sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_sent_complete", param); 
		}
	}

	@Override
	public void updateKAOSendInit(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.update("com.dhn.client.kakao.mapper.postgresql.SendRequest.req_sent_init", param); 
		} else {
			sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.req_sent_init", param); 
		}
	}

	@Override
	public void checkBackupTable(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_table_backup_1", param);
		} else {
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_table_backup_1", param);
		}
	}

	@Override
	public void createBackupTable(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_table_backup_2", param);
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_table_backup_3", param);
		} else {
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_table_backup_2", param);
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_table_backup_3", param);
		} 
	}

	@Override
	public void moveBackupTable(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_table_backup_4", param);
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.postgresql.SendRequest.req_table_backup_5", param);
		} else {
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_table_backup_4", param);
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.req_table_backup_5", param);
		}
	}

	@Override
	public void dropBackupTable(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.postgresql.SendRequest.drop_backup_table", param);
		} else {
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.drop_backup_table", param);
		}
	}

	@Override
	public int AliveCount(SQLParameter param) throws Exception {
		int cnt;
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			cnt = sqlSession.selectOne("com.dhn.client.alive.mapper.postgresql.SendRequest.alive_count", param);
		} else {
			cnt = sqlSession.selectOne("com.dhn.client.alive.mapper.SendRequest.alive_count", param);
		}
		return cnt;
	}

	@Override
	public void AliveInsert(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.insert("com.dhn.client.alive.mapper.postgresql.SendRequest.alive_insert", param);
		} else {
			sqlSession.insert("com.dhn.client.alive.mapper.SendRequest.alive_insert", param);
		}		
	}

	@Override
	public void AliveUpdate(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.update("com.dhn.client.alive.mapper.postgresql.SendRequest.alive_update", param);
		} else {
			sqlSession.update("com.dhn.client.alive.mapper.SendRequest.alive_update", param);
		}		
	}

	@Override
	public int AliveLastCount(SQLParameter param) throws Exception {
		int cnt;
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			cnt = sqlSession.selectOne("com.dhn.client.alive.mapper.postgresql.SendRequest.alive_last_count", param);
		} else {
			cnt = sqlSession.selectOne("com.dhn.client.alive.mapper.SendRequest.alive_last_count", param);
		}		
		return cnt;
	}

	@Override
	public AliveStatusBean getAliveStatus(SQLParameter param) throws Exception {
		AliveStatusBean _as;
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			_as = sqlSession.selectOne("com.dhn.client.alive.mapper.postgresql.SendRequest.alive_status", param);
		} else {
			_as = sqlSession.selectOne("com.dhn.client.alive.mapper.SendRequest.alive_status", param);
		}		
		return _as;
	}

	@Override
	public void BT_PH_Replace(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.postgresql.SendRequest.bt_ph_replace", param);
		} else {
			sqlSession.selectOne("com.dhn.client.nkakao.mapper.SendRequest.bt_ph_replace", param);
		}		
	}

	@Override
	public void AliveAlarmInsert(SQLParameter param) throws Exception {
		if(param.getDatabase() != null && param.getDatabase().toLowerCase().equals("postgresql"))
		{
			sqlSession.selectOne("com.dhn.client.alive.mapper.postgresql.SendRequest.alive_alarm_insert", param);
		} else {
			sqlSession.selectOne("com.dhn.client.alive.mapper.SendRequest.alive_alarm_insert", param);
		}	
	}
		
}
