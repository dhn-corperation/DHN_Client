package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

@Component
@Slf4j
public class ResultReq implements ApplicationListener<ContextRefreshedEvent>{
	
	public static boolean isStart = false;
	private boolean isProc = false;
	//private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	//private Map<String, String> _kaoCode = new HashMap<String,String>();
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
			
			Msg_Log _ml = new Msg_Log();
			_ml.setMsgid(ent.getString("msgid"));
			_ml.setMsg_table(msgTable);
			_ml.setLog_table(logTable);

			String restype = "";
			String rescode = "3";
			_ml.setKao_send_date(ent.getString("res_dt"));

			if(ent.getString("message_type").equalsIgnoreCase("AT")){
				_ml.setKao_err_code(ent.getString("code"));
				restype = "AT";
				if(ent.getString("code").equals("0000")){
					rescode = "3";
				}else{
					rescode = "5";
				}

				if(!ent.getString("code").equals(ent.getString("s_code"))){
					_ml.setKao_err_code(ent.getString("s_code"));
					_ml.setMsg_err_code(ent.getString("code").substring(2));
					_ml.setMsg_send_date(ent.getString("remark2"));
					restype = ent.getString("sms_kind").equalsIgnoreCase("S")?"SM":"LM";
				}
			}else{
				_ml.setKao_err_code(ent.getString("s_code"));
				restype = ent.getString("sms_kind").equalsIgnoreCase("S")?"SM":"LM";
				_ml.setMsg_err_code(ent.getString("code").substring(2));
				_ml.setMsg_send_date(ent.getString("remark2"));
				if(ent.getString("code").equals("0000")){
					rescode = "4";
				}else{
					rescode = "5";
				}
			}

			_ml.setStatus(rescode);
			_ml.setRestype(restype);
			try {
				requestService.Insert_msg_log(_ml);
			}catch (Exception e) {
				log.info("결과 처리 오류 [ " + _ml.getMsgid() + " ] - " + e.toString());
			}
		}
		log.info("결과 수신 완료 : " + json.length() + " 건");		
		procCnt--;
		
	}
	

}
