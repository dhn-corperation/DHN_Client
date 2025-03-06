package com.dhn.client.bean;


import lombok.Data;

@Data
public class Msg_Log {
	private String mseq;
	private String table;
	private String log_table;
	private String stat;
	private String result;
	private String tcprecv_type;
	private String report_time;
	private String pseq;
	private String k_next_type;
	private String ext_col1;
	private String msg_type;
	private String telecom;
	
	public Msg_Log(String _table, String _log_table) {
		this.table = _table;
		this.log_table = _log_table;
	}


}
