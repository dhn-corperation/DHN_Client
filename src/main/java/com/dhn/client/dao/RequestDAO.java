package com.dhn.client.dao;

import java.util.List;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;

public interface RequestDAO {
	public int selectSMSReqeustCount(SQLParameter param) throws Exception;
	
	public void updateSMSGroupNo(SQLParameter param) throws Exception;

	public void updateSMSSendComplete(SQLParameter param) throws Exception;

	public void updateSMSSendInit(SQLParameter param) throws Exception;

	public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception;
	
	public void Inset_msg_log(Msg_Log ml) throws Exception;
	
	public int selectMMSReqeustCount(SQLParameter param) throws Exception;
	
	public void updateMMSGroupNo(SQLParameter param) throws Exception;

	public List<RequestBean> selectMMSRequests(SQLParameter param) throws Exception;

}
