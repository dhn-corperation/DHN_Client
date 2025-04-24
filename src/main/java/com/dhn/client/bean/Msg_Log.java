package com.dhn.client.bean;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class Msg_Log {
	private String userdata;
	private String msg_seq;
	private String cur_state;
	private String sent_date;
	private String rslt_date;
	private String req_date;
	private String rslt_code;
	private String rslt_code2;
	private String rslt_net;
	private String call_to;
	private String call_from;
	private String sms_txt;
	private String msg_type;
	private String cont_seq;

	private String DBType;
	private String msg_data;
	private String mms_contents_info;
	private String msg_log;
	private String log_mv_flag;
	
	public Msg_Log(String dbtype, String md, String mci, String ml, String lmf) {
		this.DBType = dbtype;
		this.msg_data = md;
		this.mms_contents_info = mci;
		this.msg_log = ml;
		this.log_mv_flag = lmf;
		if(this.log_mv_flag.equals("DEFAULT"))
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
			LocalDateTime now = LocalDateTime.now();
			String month_table = now.format(formatter);			
			this.msg_log = this.msg_log + "_" + month_table;
		}
	}
	
	
}
