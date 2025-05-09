package com.dhn.client.controller;


import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResultReq implements ApplicationListener<ContextRefreshedEvent>{

	public static boolean isStart = false;
	public static boolean isProc = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	private Map<String, String> _rsltCode = new HashMap<String, String>();
	private static int procCnt = 0;
	private String dual;
	private static String role;
	
	private static final Logger log = LogManager.getRootLogger();
	
	@Autowired
	private RequestService reqService;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// TODO Auto-generated method stub
		param.setMsg_table( appContext.getEnvironment().getProperty("dhnclient.msg_table") );
		param.setKakao( appContext.getEnvironment().getProperty("dhnclient.kakao") );
		param.setDatabase(appContext.getEnvironment().getProperty("dhnclient.database"));
		
		dhnServer = "http://" + appContext.getEnvironment().getProperty("dhnclient.server") + "/";
		userid = appContext.getEnvironment().getProperty("dhnclient.userid");
		dual =  appContext.getEnvironment().getProperty("dhnclient.dual");
		role = appContext.getEnvironment().getProperty("dhnclient.role");

		if(dual != null && dual.toUpperCase().equals("Y")) {
			//if(role != null && role.toUpperCase().equals("MASTER") ) {
			//	isStart = true;
			//}
		} else {
			isStart = true;	
		}
		
	}
	
	@Scheduled(fixedDelay = 100)
	private void SendProcess() {
		if(isStart && !isProc && procCnt < 10) {
			isProc = true;
			procCnt++;
			try {
				ObjectMapper om = new ObjectMapper();
				
				HttpHeaders header = new HttpHeaders();
				
				header.setContentType(MediaType.APPLICATION_JSON);
				header.set("userid", userid);
				
				RestTemplate rt = new RestTemplate();
				HttpEntity<String> entity = new HttpEntity<String>(null, header);
				
				try {
					ResponseEntity<String> response = rt.postForEntity(dhnServer + "result", entity, String.class);
											
					if(response.getStatusCode() ==  HttpStatus.OK)
					{
						JSONArray json = new JSONArray(response.getBody().toString());
						if(json.length()>0) {
							Thread res = new Thread(() ->ResultProc(json, procCnt) );
							res.start();
						} else {
							procCnt--;	
						}
					} else {
						procCnt--;
					}
				} catch(Exception ex) {
					log.info("결과 수신 오류 : " + ex.toString());
					procCnt--;
				}
				
			} catch (Exception e) {  
				// TODO Auto-generated catch block
				//e.printStackTrace();
				log.info("결과 수신 오류 : " + e.toString());
				procCnt--;
			}
			
			isProc = false;
		}
	}

	private void ResultProc(JSONArray json, int _pc) {
		
		for(int i=0; i<json.length();i++) {
			JSONObject ent = json.getJSONObject(i);
			Msg_Log _ml = new Msg_Log(param.getMsg_table(), param.getKakao());
			_ml.setCmp_msg_id(ent.getString("msgid"));
			_ml.setDatabase(param.getDatabase());
			
			String rscode = "06";
			
			if(param.getKakao() != null && param.getKakao().toUpperCase().equals("Y")) {
				_ml.setKakao(true);
			}  else {
				_ml.setKakao(false);
			}

			if(ent.getString("message_type").toUpperCase().equals("PH")) 
			{
				if(!ent.getString("code").equals("0000")) {
					rscode = ent.getString("code").substring(2);
					_ml.setSms_st("4");
				} else {
					_ml.setSms_st("2");
				}
				_ml.setCmp_rcv_dttm(ent.getString("remark2"));
				_ml.setRcv_mno_cd(ent.getString("remark1"));
				_ml.setKmsg_rslt(ent.getString("s_code"));
			} else {
				if(!ent.getString("code").equals("0000")) {
					rscode = ent.getString("code").substring(2);
					_ml.setSms_st("4");
				} else {
					_ml.setSms_st("2");
				}
				_ml.setKmsg_rslt(ent.getString("code"));
				_ml.setCmp_rcv_dttm(ent.getString("res_dt"));
				
				if(ent.getString("message_type").toUpperCase().startsWith("A")) 
				{
					_ml.setRcv_mno_cd("AT");
				} else {
					_ml.setRcv_mno_cd("FT");
				}
			}
			_ml.setRslt_val(rscode);
			if(_ml.getRcv_mno_cd().length()>=2) {
				_ml.setRcv_mno_cd(_ml.getRcv_mno_cd().substring(0,1));
			}
			try {
				reqService.Inset_msg_log(_ml);
			} catch (Exception e) {
				log.info("[ " + ent.getString("msgid") + " ] 결과 처리중 오류 발생 / " + e.getMessage());
			}
			if(_ml.isKakao()) {
				log.info("[ " + ent.getString("msgid") + " ] 결과 수신완료 - sms_st=" + _ml.getSms_st() + ",rslt_val=" +_ml.getRslt_val() +",cmp_rcv_dttm=" +_ml.getCmp_rcv_dttm()+",rcv_mno_cd="+_ml.getRcv_mno_cd()+",reg_rcv_dttm=sysdate,kmsg_rslt=" +_ml.getKmsg_rslt());
			}else {
				log.info("[ " + ent.getString("msgid") + " ] 결과 수신완료 - sms_st=" + _ml.getSms_st() + ",rslt_val=" +_ml.getRslt_val() +",cmp_rcv_dttm=" +_ml.getCmp_rcv_dttm()+",rcv_mno_cd="+_ml.getRcv_mno_cd()+",reg_rcv_dttm=sysdate");
			}
			
		}
		procCnt--;
	}
	
	static public void setIsStart(boolean _flag) {
		log.info(role + " Result Request is  change : " + _flag);
		isStart = _flag;
	}
 
}

