package com.dhn.client.bean;

public class SQLParameter {
	private String msg_table;
	private String img_table;
	private String kakao;
	private String group_no;
	private String msg_type;
	private String log_mv_flag;
	private String bktable;
	private String send_msg_limit;
	private String database;
	private String role;
	private String alive_status;
	private String snd_dttm;
	private String msgid;
	private String mms_key;
	
	public String getSnd_dttm() {
		return snd_dttm;
	}

	public void setSnd_dttm(String snd_dttm) {
		this.snd_dttm = snd_dttm;
	}

	private boolean otpFlag = false;
	
	public SQLParameter() {
		// TODO Auto-generated constructor stub
		this.database = "";
	}
	
	public String getMsg_table() {
		return msg_table;
	}
	public void setMsg_table(String msg_table) {
		this.msg_table = msg_table;
	}
	public String getImg_table() {
		return img_table;
	}
	public void setImg_table(String img_table) { this.img_table = img_table; }
	public String getKakao() {
		return kakao;
	}
	public void setKakao(String kakao) {
		this.kakao = kakao;
	}
	public String getGroup_no() {
		return group_no;
	}
	public void setGroup_no(String group_no) {
		this.group_no = group_no;
	}
	public String getMsg_type() {
		return msg_type;
	}
	public void setMsg_type(String msg_type) {
		this.msg_type = msg_type;
	}
	public String getLog_mv_flag() {
		return log_mv_flag;
	}
	public void setLog_mv_flag(String log_mv_flag) {
		this.log_mv_flag = log_mv_flag;
	}
	public String getBktable() {
		return bktable;
	}
	public void setBktable(String bktable) {
		this.bktable = bktable;
	}
	public boolean isOtpFlag() {
		return otpFlag;
	}
	public void setOtpFlag(boolean otpFlag) {
		this.otpFlag = otpFlag;
	}
	public String getSend_msg_limit() {
		return send_msg_limit;
	}
	public void setSend_msg_limit(String send_msg_limit) {
		this.send_msg_limit = send_msg_limit;
	}
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getAlive_status() {
		return alive_status;
	}
	public void setAlive_status(String alive_status) {
		this.alive_status = alive_status;
	}
	public String getMsgid() {
		return msgid;
	}
	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}
	public String getMms_key() { return mms_key;}
	public void setMms_key(String mms_key) { this.mms_key = mms_key;}
	

}
