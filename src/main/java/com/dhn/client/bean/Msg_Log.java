package com.dhn.client.bean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Msg_Log {
	private String msg_table;
	private String log_table;
	private String kakao;
	private String tran_pr;
	private String reg_rcv_dttm;
	private String sms_st;
	private String rslt_val;
	private String cmp_rcv_dttm;
	private String rcv_mno_cd; 
	private String kmsg_rslt;
	private String replace_type;
	private String msgType;
	private String sdk_table;
	private String resultmsg;
	private String regdate;
	private String newagent;
	private String sdk_st;
	
	public String getResultmsg() {
		return resultmsg;
	}

	public void setResultmsg(String resultmsg) {
		this.resultmsg = resultmsg;
	}

	public String getRegdate() {
		return regdate;
	}

	public void setRegdate(String regdate) {
		this.regdate = regdate.replaceAll("[^0-9]", ""); 
	}

	public String getSdk_table() {
		return sdk_table;
	}

	public void setSdk_table(String sdk_table) {
		this.sdk_table = sdk_table;
	}

	public String getReplace_type() {
		return replace_type;
	}

	public void setReplace_type(String replace_type) {
		this.replace_type = replace_type;
	}

	public Msg_Log(String md, String kao) {
		this.msg_table = md;
		this.kakao = kao;
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

	public String getLog_table() {
		return log_table;
	}

	public void setLog_table(String log_table) {
		this.log_table = log_table;
	}

	public String getTran_pr() {
		return tran_pr;
	}

	public void setTran_pr(String tran_pr) {
		this.tran_pr = tran_pr;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getNewagent() {
		if(this.newagent != null ) {
			return newagent;
		} else {
			return "N";
		}
	}

	public void setNewagent(String newagent) {
		if(newagent != null && newagent.length() > 0) {
			this.newagent = newagent.toUpperCase();
		} else {
			this.newagent = "N";
		}
	}

	public String getSdk_st() {
		return sdk_st;
	}

	public void setSdk_st(String sdk_st) {
		this.sdk_st = sdk_st;
	}


}
