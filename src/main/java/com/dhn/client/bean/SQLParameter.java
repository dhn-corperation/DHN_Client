package com.dhn.client.bean;

public class SQLParameter {
	private String msg_table;
	private String kakao;
	private String group_no;
	private String msg_type;
	private String log_mv_flag;
	private String bktable;
	private String kakaobtn;
	private String newagent;
	
	public String getNewagent() {
		return newagent;
	}
	public void setNewagent(String newagent) {
		if(newagent != null && newagent.length() > 0) {
			this.newagent = newagent;
		} else {
			this.newagent = "N";
		}		 
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
	public String getKakaobtn() {
		return kakaobtn;
	}
	public void setKakaobtn(String kakaobtn) {
		if(kakaobtn != null && kakaobtn.length() > 0) {
			this.kakaobtn = kakaobtn.toUpperCase();
		} else {
			this.kakaobtn = "N";
		}
	}
	
 
	

}
