package com.dhn.client.bean;

import lombok.Data;

@Data
public class Msg_Log {
	private String msg_table;
	private String log_table;
	private String main_table;
	private String main_log_table;
	private String msgid;
	private String msg_type;
	private String real_send_date;
	private String response_date;
	private String result_code;
	private String result_msg;
	private String mod_id;
	private String flag_2nd;
	private String log_date_table;
	private String source_err_msg;

	
	public Msg_Log(String msg_table, String log_table, String main_table, String main_log_table) {
		this.msg_table = msg_table;
		this.log_table = log_table;
		this.main_table = main_table;
		this.main_log_table = main_log_table;
	}

}
