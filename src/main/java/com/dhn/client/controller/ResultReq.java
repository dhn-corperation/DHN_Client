package com.dhn.client.controller;

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

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ResultReq implements ApplicationListener<ContextRefreshedEvent>{
	
	public static boolean isStart = false;
	private boolean isProc = false;
	private String dhnServer;
	private String userid;
	private static int procCnt = 0;
	private String msgTable = "";
	private String logTable = "";

	@Autowired
	private RequestService requestService;
	
	@Autowired
	private ApplicationContext appContext;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		
		msgTable = appContext.getEnvironment().getProperty("dhnclient.msg_table");
		logTable = appContext.getEnvironment().getProperty("dhnclient.log_table");
		
		dhnServer = appContext.getEnvironment().getProperty("dhnclient.dhn_kakao_server") + "/";
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

						String responseBody = response.getBody();
						JSONObject jsonObject = new JSONObject(responseBody);

						if (jsonObject.has("data")) {
							JSONObject dataObject = jsonObject.getJSONObject("data");

							if (dataObject.has("detail")) {
								JSONArray jsonArray = dataObject.getJSONArray("detail");

								if (jsonArray.length() > 0) {
									Thread res = new Thread(() -> ResultProc(jsonArray, procCnt));
									res.start();
								} else {
									Thread.sleep(5000);
									procCnt--;
								}
							} else {
								log.error("결과 수신 오류 : 결과 배열(detail)이 없습니다.");
								procCnt--;
							}
						} else {
							log.error("결과 수신 오류 : (data) 필드가 없습니다.");
							procCnt--;
						}

//						JSONArray json = new JSONArray(response.getBody().toString());
//						if(json.length()>0) {
//							Thread res = new Thread(() ->ResultProc(json, procCnt) );
//							res.start();
//						} else {
//							procCnt--;
//						}

					} else {
						log.info("결과 수신 오류 (Http Err) : " + response.getStatusCode());
						procCnt--;
					}
				} catch(Exception ex) {
					log.info("결과 수신 오류 (response Err): " + ex.toString());
					procCnt--;
				}
				
			}catch (Exception e) {
				log.info("결과 수신 오류 : " + e.toString());
				procCnt--;
			}
			isProc = false;
		}
	}


	private void ResultProc(JSONArray json, int _pc) {
		
		for(int i=0; i<json.length(); i++) {
			JSONObject ent = json.getJSONObject(i);
			
			Msg_Log _ml = new Msg_Log(msgTable, logTable);
			_ml.setMsgid(ent.getString("msgid"));

			_ml.setMsg_type(ent.getString("message_type").toUpperCase());

			if(ent.getString("message_type").equalsIgnoreCase("AP")){ // 앱푸쉬 결과 처리
				_ml.setSend_type("P");
				if(ent.getString("code").equals("0000")){
					_ml.setCode("4");
				}else{
					_ml.setCode(ent.getString("code"));
				}
				_ml.setReal_send_date(ent.getString("res_dt"));
				_ml.setTel_code("0");
			} else if(ent.getString("message_type").equalsIgnoreCase("AT")) { // 알림톡 결과 처리
				_ml.setSend_type("K");
				if(ent.getString("s_code").equals("0000")){
					_ml.setCode("7000");
				}else{
					_ml.setCode(ent.getString("s_code"));
				}
				_ml.setReal_send_date(ent.getString("res_dt"));
				_ml.setTel_code("0");
			}else if(ent.getString("message_type").equalsIgnoreCase("PH")){ // 문자 결과 처리
				_ml.setSend_type(ent.getString("sms_kind").toUpperCase());
				if(ent.getString("code").equals("0000")){
					_ml.setCode("1000");
				}else{
					_ml.setCode(ent.getString("code"));
				}
				if(ent.getString("remark1").equalsIgnoreCase("LGT") || ent.getString("remark1").equals("019")){
					_ml.setTel_code("19");
				}else if(ent.getString("remark1").equalsIgnoreCase("SKT") || ent.getString("remark1").equals("011")){
					_ml.setTel_code("11");
				}else if(ent.getString("remark1").equalsIgnoreCase("KTF") || ent.getString("remark1").equalsIgnoreCase("KT") || ent.getString("remark1").equals("016")){
					_ml.setTel_code("16");
				}else{
					_ml.setTel_code("0");
				}
				_ml.setReal_send_date(ent.getString("remark2"));
			}

			try {
				requestService.update_msg_log(_ml);
			}catch (Exception e) {
				log.info("결과 처리 오류 [ " + _ml.getMsgid() + " ] - " + e.toString());
			}
		}
		log.info("결과 수신 완료 : " + json.length() + " 건");		
		procCnt--;
		
	}

}
