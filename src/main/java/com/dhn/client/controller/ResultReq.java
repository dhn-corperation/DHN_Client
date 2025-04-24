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
import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ResultReq implements ApplicationListener<ContextRefreshedEvent>{

	public static boolean isStart = false;
	private boolean isProc = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	private Map<String, String> _rsltCode = new HashMap<String, String>();

	private static String role = "";
	private String dual = "N";
	
	@Autowired
	private RequestService requestService;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// TODO Auto-generated method stub
		param.setDBType( appContext.getEnvironment().getProperty("dhnclient.database") );
		param.setMsg_data( appContext.getEnvironment().getProperty("dhnclient.msg_data") );
		param.setMms_contents_info( appContext.getEnvironment().getProperty("dhnclient.mms_contents_info") );
		param.setMsg_log( appContext.getEnvironment().getProperty("dhnclient.msg_log") );
		param.setLog_mv_flag( appContext.getEnvironment().getProperty("dhnclient.npro_logmakemode") );
		//param.setSTART_TIME( Integer.parseInt( appContext.getEnvironment().getProperty("dhnclient.start_time") ) );
		//param.setEND_TIME( Integer.parseInt( appContext.getEnvironment().getProperty("dhnclient.end_time") ) );

		dual = appContext.getEnvironment().getProperty("dhnclient.dual","N");
		role = appContext.getEnvironment().getProperty("dhnclient.role");

		dhnServer = "https://" + appContext.getEnvironment().getProperty("dhnclient.server") + "/";
		userid = appContext.getEnvironment().getProperty("dhnclient.userid");
		
		_rsltCode.put("03","e");
		_rsltCode.put("05","3");
		_rsltCode.put("06","0");
		_rsltCode.put("07","a");
		_rsltCode.put("08","C");
		_rsltCode.put("09","B");
		_rsltCode.put("10","D");
		_rsltCode.put("11","d");
		_rsltCode.put("13","k");
		_rsltCode.put("14","k");
		_rsltCode.put("15","k");
		_rsltCode.put("16","k");
		_rsltCode.put("20","h");
		_rsltCode.put("21","a");
		_rsltCode.put("22","c");
		_rsltCode.put("23","h");
		_rsltCode.put("28","g");
		_rsltCode.put("29","b");
		_rsltCode.put("36","2");
		_rsltCode.put("37","2");
		_rsltCode.put("38","n");
		_rsltCode.put("50","F");
		_rsltCode.put("51","G");
		_rsltCode.put("52","H");
		_rsltCode.put("53","I");
		_rsltCode.put("54","J");
		_rsltCode.put("59","d");
		_rsltCode.put("60","o");
		_rsltCode.put("61","p");
		_rsltCode.put("69","d");
		_rsltCode.put("73","x");
		_rsltCode.put("74","d");
		_rsltCode.put("75","1");
		_rsltCode.put("76","2");
		_rsltCode.put("77","2");
		_rsltCode.put("78","x");
		_rsltCode.put("79","d");
		_rsltCode.put("90","1");
		_rsltCode.put("91","z");
		_rsltCode.put("92","d");
		_rsltCode.put("93","n");
		_rsltCode.put("94","n");
		_rsltCode.put("95","n");
		_rsltCode.put("96","j");
		_rsltCode.put("97","7");
		_rsltCode.put("98","8");
		_rsltCode.put("99","9"); 
		
		isStart = true;

		if(dual.equalsIgnoreCase("Y")){

		}else{
			log.info("RESULT 초기화 완료 됨. - " + param.getDBType() );
			isStart = true;
		}
	}
	
	@Scheduled(fixedDelay = 1000)
	private void SendProcess() {
		if(isStart && !isProc) {
			isProc = true;
			
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
						/*
						JSONArray json = new JSONArray(response.getBody().toString());
						if(json.length()>0) {
							
							//log.info(response.getBody().toString());
							for(int i=0; i<json.length();i++) {
								JSONObject ent = json.getJSONObject(i);
								Msg_Log _ml = new Msg_Log(param.getDBType(), param.getMsg_data(), param.getMms_contents_info(), param.getMsg_log(), param.getLog_mv_flag());
								_ml.setUserdata(ent.getString("p_invoice"));
								_ml.setMsg_seq(ent.getString("msgid"));
								_ml.setRslt_date(ent.getString("remark2"));
								String rscode = "06";
								if(!ent.getString("code").equals("0000")) {
									rscode = ent.getString("code").substring(2);
								}
								_ml.setRslt_code(rscode);
								_ml.setRslt_code2(_rsltCode.get(rscode));
								_ml.setRslt_net(ent.getString("remark1"));
								
								requestService.Inset_msg_log(_ml);
							}
							
							log.info("결과 수신 완료 : " + json.length() + " 건");
						}
						*/
						String responseBody = response.getBody();
						JSONObject jsonObject = new JSONObject(responseBody);

						if (jsonObject.has("data")) {
							JSONObject dataObject = jsonObject.getJSONObject("data");

							if (dataObject.has("detail")) {
								JSONArray json = dataObject.getJSONArray("detail");

								if (json.length() > 0) {
									for(int i=0; i<json.length();i++) {
										JSONObject ent = json.getJSONObject(i);
										Msg_Log _ml = new Msg_Log(param.getDBType(), param.getMsg_data(), param.getMms_contents_info(), param.getMsg_log(), param.getLog_mv_flag());
										_ml.setUserdata(ent.getString("p_invoice"));
										_ml.setMsg_seq(ent.getString("msgid"));
										_ml.setRslt_date(ent.getString("remark2"));
										String rscode = "06";
										if(!ent.getString("code").equals("0000")) {
											rscode = ent.getString("code").substring(2);
										}
										_ml.setRslt_code(rscode);
										_ml.setRslt_code2(_rsltCode.get(rscode));
										_ml.setRslt_net(ent.getString("remark1"));

										requestService.Inset_msg_log(_ml);
									}

									log.info("결과 수신 완료 : " + json.length() + " 건");
								}
							} else {
								log.error("결과 수신 오류 : 결과 배열(detail)이 없습니다.");
							}
						} else {
							log.error("결과 수신 오류 : (data) 필드가 없습니다.");
						}
					}
				} catch(Exception ex) {
					log.info("결과 수신 오류 : " + ex.toString());
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			isProc = false;
		}
	}

	static public void setIsStart(boolean _flag) {
		log.info(role + " Result Request is  change : " + _flag);
		isStart = _flag;
	}
}

