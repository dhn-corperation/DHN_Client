package com.dhn.client.bean;

public class Msg_Log {
	private String msg_table;
	private String kakao;
	private String cmp_msg_id;
	private String reg_rcv_dttm;
	private String sms_st;
	private String rslt_val;
	private String cmp_rcv_dttm;
	private String rcv_mno_cd; 
	private String kmsg_rslt;
	private boolean isKakao = false;
	private String database;
	
	public Msg_Log(String md, String kao) {
		this.msg_table = md;
		this.kakao = kao;
		this.database = "";
	}

	public String getCmp_msg_id() {
		return cmp_msg_id;
	}

	public void setCmp_msg_id(String cmp_msg_id) {
		this.cmp_msg_id = cmp_msg_id;
	}

	public String getMsg_table() {
		return msg_table;
	}

	public void setMsg_table(String msg_table) {
		this.msg_table = msg_table;
	}

	public String getKakao() {
		return kakao;
	}

	public void setKakao(String kakao) {
		this.kakao = kakao;
	}

	public String getReg_rcv_dttm() {
		return reg_rcv_dttm;
	}

	public void setReg_rcv_dttm(String reg_rcv_dttm) {
		this.reg_rcv_dttm = reg_rcv_dttm.replaceAll("[^0-9]", "");
	}

	public String getSms_st() {
		return sms_st;
	}

	public void setSms_st(String sms_st) {
		this.sms_st = sms_st;
	}

	public String getRslt_val() {
		return rslt_val;
	}

	public void setRslt_val(String rslt_val) {
		this.rslt_val = rslt_val;
	}

	public String getCmp_rcv_dttm() {
		return cmp_rcv_dttm;
	}

	public void setCmp_rcv_dttm(String cmp_rcv_dttm) {
		this.cmp_rcv_dttm = cmp_rcv_dttm.replaceAll("[^0-9]", "");
	}

	public String getRcv_mno_cd() {
		return rcv_mno_cd;
	}

	public void setRcv_mno_cd(String rcv_mno_cd) {
		this.rcv_mno_cd = rcv_mno_cd;
	}

	public String getKmsg_rslt() {
		return kmsg_rslt;
	}

	public void setKmsg_rslt(String kmsg_rslt) {
		this.kmsg_rslt = kmsg_rslt;
	}

	public boolean isKakao() {
		return isKakao;
	}

	public void setKakao(boolean isKakao) {
		this.isKakao = isKakao;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}


}
