package com.dhn.client.bean;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class SQLParameter {
	private String msg_table;
	private String log_table;
	private String tran_msg_table;
	private String tran_log_table;
	private String mms_msg_table;
	private String mms_log_table;
	private String kakao_use;
	private String sms_use;
	private String lms_use;
	private String mms_use;
	private String dbtype;
	private String group_no;
	private String msg_type;
	private String log_mv_flag;
	private String bktable;
	private String kakaobtn;
	private String newagent;
	private String dist_proc_option;
	private String dist_value;
	private String att_file_path;
	private String file1;
	private String file2;
	private String file3;
    private String profile_key;
	private String mms_key;
    private String onlysms;

	public void setDist_proc_option(String dist_proc_option) {
		if(dist_proc_option != null && dist_proc_option.length() > 0) {
			this.dist_proc_option = dist_proc_option;
		} else {
			this.dist_proc_option = "N";
		}
	}

	public void setNewagent(String newagent) {
		if(newagent != null && newagent.length() > 0) {
			this.newagent = newagent;
		} else {
			this.newagent = "N";
		}		 
	}

    // onlysms 필드용 커스텀 setter
    public void setOnlysms(String onlysms) {
        if (StringUtils.hasText(onlysms)) {
            this.onlysms = onlysms;
        } else {
            this.onlysms = "Y";  // 빈값이거나 공백일 경우 "Y" 설정
        }
    }

	public void setKakaobtn(String kakaobtn) {
		if(kakaobtn != null && kakaobtn.length() > 0) {
			this.kakaobtn = kakaobtn.toUpperCase();
		} else {
			this.kakaobtn = "N";
		}
	}	

}
