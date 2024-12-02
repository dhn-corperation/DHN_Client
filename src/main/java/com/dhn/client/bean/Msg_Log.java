package com.dhn.client.bean;

import lombok.Data;

@Data
public class Msg_Log {
	private String msg_table;
	private String log_table;
	private String msgid;
	private String msg_type;
	private String send_type;

	
	public Msg_Log(String msg_table, String log_table) {
		this.msg_table = msg_table;
		this.log_table = log_table;
	}

}
