package com.dhn.client.service;

import java.util.List;

import com.dhn.client.bean.*;

public interface RequestService {

	// 알림톡
	int selectKAORequestCount(SQLParameter param) throws Exception;

	List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception;

	void updateKAOSendComplete(SQLParameter param) throws Exception;

	void updateKAOSendInit(SQLParameter param) throws Exception;

	int selectRealKAORequestCount(SQLParameter param) throws Exception;

	List<KAORequestBean> selectRealKAORequests(SQLParameter param) throws Exception;

	// 문자
	int selectMSGRequestCount(SQLParameter param) throws Exception;

	List<RequestBean> selectMSGRequests(SQLParameter param) throws Exception;

	void updateMSGSendComplete(SQLParameter param) throws Exception;

	void updateMSGSendInit(SQLParameter param) throws Exception;

	int selectRealMSGRequestCount(SQLParameter param) throws Exception;

	List<RequestBean> selectRealMSGRequests(SQLParameter param) throws Exception;

	// 결과 처리
	void update_msg_log(Msg_Log ml) throws Exception;

	// 테이블 생성
	void logTableCheck(String msg_table, String log_table) throws Exception;

	void tableCheck(SQLParameter param) throws Exception;

    void phnErrUpdateDelete(Msg_Log ml) throws Exception;

    void sourceErrUpdate(Msg_Log ml) throws Exception;

	// 그룹핑
	void kaoGroupUpdate(SQLParameter param) throws Exception;

	void msgGroupUpdate(SQLParameter param) throws Exception;

	void kaoRealGroupUpdate(SQLParameter param) throws Exception;

	void msgRealGroupUpdate(SQLParameter param) throws Exception;

}
