package com.dhn.client.bean;

import lombok.Data;

@Data
public class Msg_Log {
	private String msg_table;
	private String log_table;
	private String msgid;
	private String msgtype;
	private String status;
	private String restype;
	private String kao_err_code;
	private String kao_send_date;
	private String msg_err_code;
	private String msg_send_date;

}
