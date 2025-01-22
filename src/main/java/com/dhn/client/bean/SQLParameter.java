package com.dhn.client.bean;

import lombok.Data;

import java.util.List;

@Data
public class SQLParameter {
	private String msg_table;
	private String log_table;
	private String tmp_table;
	private String main_table;
	private String alive_table;
	private String btn_table;
	private String mod_id;
	private String kakao_use;
	private String sms_use;
	private String lms_use;
	private String smslms_use;
	private String tmp_use;
	private String group_no;
	private String msg_type;
	private String bdpt_profile_key;
	private String insure_profile_key;
	private String nps_profile_key;
	private String file1;
	private String file2;
	private String file3;
	private String tmplid;
	private String msgid;
	private String tmplstatus;
	private String rej_memo;
	private String role;
	private String role_type;
	private String alive_status;
	private List<String> msgid_list;
}
