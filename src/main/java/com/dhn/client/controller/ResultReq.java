package com.dhn.client.controller;


import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.dhn.client.bean.SQLParameter;
import com.dhn.client.bean.LMSTableBean;
import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
public class ResultReq implements ApplicationListener<ContextRefreshedEvent>{

	public static boolean isStart = false;
	private boolean isProc = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	private Map<String, String> _rsltCode = new HashMap<String, String>();
	private static int procCnt = 0;
	private String reqTable = "";
	private String logTable = "";
 
	
	private static final Logger log = LogManager.getRootLogger();
	
	@Autowired
	private RequestService reqService;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// TODO Auto-generated method stub
		reqTable = appContext.getEnvironment().getProperty("dhnclient.req_table");
		logTable = appContext.getEnvironment().getProperty("dhnclient.log_table");
		
		dhnServer = "http://" + appContext.getEnvironment().getProperty("dhnclient.server") + "/";
		userid = appContext.getEnvironment().getProperty("dhnclient.userid");
		
		isStart = true;
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
				log.info("결과 수신 오류 : " + e.toString());
				procCnt--;
			}
			
			isProc = false;
		}
	}
	
	private void ResultProc(JSONArray json, int _pc) {
		
		//log.info(response.getBody().toString());
		int _procPoint = 0;
		for(int i=0; i<json.length();i++) {
			JSONObject ent = json.getJSONObject(i);
			
			Msg_Log _ml = new Msg_Log(reqTable, logTable, ent.getString("reg_dt"));
			
			_ml.setCmid(ent.getString("msgid"));
			
			if(!ent.getString("code").equals("0000")) {
				_ml.setSms_st("3");
			} else {
				_ml.setSms_st("2");
			}
			
			_ml.setRslt_val(ent.getString("code"));

			if(ent.getString("message_type").equalsIgnoreCase("AT")){
				_ml.setCmp_rcv_dttm(ent.getString("res_dt"));
				_ml.setRcv_mno_cd("KKO");
			}else{
				_ml.setCmp_rcv_dttm(ent.getString("remark2"));
				if(ent.getString("remark1").equalsIgnoreCase("LGT") || ent.getString("remark1").equals("019")){
					_ml.setRcv_mno_cd("LGT");
				}else if(ent.getString("remark1").equalsIgnoreCase("SKT") || ent.getString("remark1").equals("011")){
					_ml.setRcv_mno_cd("SKT");
				}else if(ent.getString("remark1").equalsIgnoreCase("KTF") || ent.getString("remark1").equalsIgnoreCase("KT") || ent.getString("remark1").equals("016")){
					_ml.setRcv_mno_cd("KTF");
				}else{
					_ml.setRcv_mno_cd(ent.getString("remark1"));
				}
			}
			
			try {
				reqService.Insert_msg_log(_ml);
			} catch (Exception e) {
				log.info("결과 처리 오류 [ " + _ml.getCmid() + " ] - " + e.toString());
			}
		}
		
		log.info("결과 수신 완료 : " + json.length() + " 건");		
		procCnt--;
	}
}

