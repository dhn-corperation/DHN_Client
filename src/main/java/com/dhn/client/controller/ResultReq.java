package com.dhn.client.controller;

import com.dhn.client.bean.LMSTableBean;
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

import java.io.UnsupportedEncodingException;
import java.util.List;

@Component
@Slf4j
public class ResultReq implements ApplicationListener<ContextRefreshedEvent>{
	
	public static boolean isStart = false;
	private boolean isProc = false;
	//private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	//private Map<String, String> _rsltCode = new HashMap<String, String>();
	private static int procCnt = 0;
	private String msgTable = "";
	private String tableseq = "";
	
	@Autowired
	private RequestService requestService;
	
	@Autowired
	private ApplicationContext appContext;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		
		msgTable = appContext.getEnvironment().getProperty("dhnclient.msg_table");
		tableseq = appContext.getEnvironment().getProperty("dhnclient.table_seq");
		
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

			String rscode = "0000";
			_ml.setStatus("2");

			if(ent.getString("message_type").toUpperCase().equals("PH")){
				rscode = ent.getString("code");
				_ml.setResult(rscode);
				_ml.setResult_time(ent.getString("remark2"));

				if (!rscode.equals("0000")){
					_ml.setStatus("3");
				}
			}else{
				_ml.setResult(ent.getString("code"));
				_ml.setResult_time(ent.getString("res_dt"));

				if(!_ml.getResult().equals("0000")){
					_ml.setStatus("3");
					try{
						LMSTableBean lmsBean = requestService.kakao_to_sms_select(_ml);
						if(lmsBean != null){

							lmsBean.setTable(msgTable);
							lmsBean.setTable_seq(tableseq);

							// SMS가 90자 초과일 경우 LMS로 변경
							if(lmsBean.getSmskind().equals("S")){
								if(lmsBean.getMsgsms().length()>90){
									lmsBean.setSmskind("M");
								}
							}

							requestService.insert_sms(lmsBean);
						}

					}catch (Exception e){
						e.printStackTrace();
					}
				}
			}
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
