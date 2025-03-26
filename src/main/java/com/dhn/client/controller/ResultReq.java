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
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ResultReq implements ApplicationListener<ContextRefreshedEvent>{
	
	public static boolean isStart = false;
	private boolean isProc = false;
	private String dhnServer;
	private String userid;
	private static int procCnt = 0;
	private Map<String, String> _msgCode = new HashMap<>();
	private Map<String, String> _kaoCode = new HashMap<>();
	private String msgTable = "";
	private String logTable = "";
	private String mainTable = "";
	private String mainLogTable = "";
	private String mod_id = "";
	private String dual;
	private static String role;

	@Autowired
	private RequestService requestService;
	
	@Autowired
	private ApplicationContext appContext;

	@Autowired
	ScheduledAnnotationBeanPostProcessor posts;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		
		msgTable = appContext.getEnvironment().getProperty("dhnclient.msg_table");
		logTable = appContext.getEnvironment().getProperty("dhnclient.log_table");
		mainTable = appContext.getEnvironment().getProperty("dhnclient.main_table");
		mainLogTable = appContext.getEnvironment().getProperty("dhnclient.main_log_table");
		dhnServer = appContext.getEnvironment().getProperty("dhnclient.dhn_kakao_server");
		mod_id = appContext.getEnvironment().getProperty("dhnclient.mod_id");
		userid = appContext.getEnvironment().getProperty("dhnclient.userid");
		dual = appContext.getEnvironment().getProperty("dhnclient.dual");
		role = appContext.getEnvironment().getProperty("dhnclient.role");

		_kaoCode.put("0000","0000");
		_kaoCode.put("3000","2001");
		_kaoCode.put("1006","3005");
		_kaoCode.put("1001","3023");
		_kaoCode.put("1003","3024");
		_kaoCode.put("3012","3030");
		_kaoCode.put("3013","3031");
		_kaoCode.put("3014","3032");
		_kaoCode.put("3015","3033");
		_kaoCode.put("3016","3034");
		_kaoCode.put("1002","3040");
		_kaoCode.put("1004","3041");
		_kaoCode.put("1007","3044");
		_kaoCode.put("1011","3048");
		_kaoCode.put("3006","3049");
		_kaoCode.put("3019","3050");
		_kaoCode.put("3005","3060");
		_kaoCode.put("1012","3062");
		_kaoCode.put("1030","3063");
		_kaoCode.put("9998","9998");
		_kaoCode.put("9999","9999");
		_kaoCode.put("3008","1002");
		_kaoCode.put("3018","E999");

		_msgCode.put("0000","0000");
		_msgCode.put("7003","2100");
		_msgCode.put("7050","2101");
		_msgCode.put("7028","2103");
		_msgCode.put("7060","2104");
		_msgCode.put("7087","2106");
		_msgCode.put("7086","2107");
		_msgCode.put("7022","232");
		_msgCode.put("7001","233");
		_msgCode.put("7095","249");
		_msgCode.put("7093","250");
		_msgCode.put("7061","263");
		_msgCode.put("7055","408");
		_msgCode.put("7015","101");
		_msgCode.put("7013","102");
		_msgCode.put("7014","103");
		_msgCode.put("7056","108");
		_msgCode.put("7057","112");
		_msgCode.put("7084","113");
		_msgCode.put("7053","114");
		_msgCode.put("7088","115");
		_msgCode.put("7051","116");
		_msgCode.put("7023","201");
		_msgCode.put("7008","204");
		_msgCode.put("7009","205");
		_msgCode.put("7010","206");
		_msgCode.put("7005","213");
		_msgCode.put("7076","216");
		_msgCode.put("7098","39");
		_msgCode.put("7099","1");
		_msgCode.put("7078","21");
		_msgCode.put("7075","94");
		_msgCode.put("7096","4008");
		_msgCode.put("7074","4306");
		_msgCode.put("7021","4307");
		_msgCode.put("7029","5300");
		_msgCode.put("7011","8011");

		if(dual != null && dual.equalsIgnoreCase("Y")){

		} else {
			isStart = true;
			log.info("Result 초기화 완료");
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

					} else {
						log.info("결과 수신 오류 (Http Err) : " + response.getStatusCode());

						procCnt--;
					}
				} catch(Exception ex) {
					log.info("결과 수신 오류 (response Err): " + ex.toString());
					Thread.sleep(10000);
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

		try {
			requestService.logTableCheck(msgTable, logTable);
		} catch (Exception e) {
			log.error("테이블 확인 및 생성 실패: " + e.getMessage());
		}

		LocalDate now = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
		String currentMonth = now.format(formatter);

		List<String> msg_list = new ArrayList<String>();
		
		for(int i=0; i<json.length(); i++) {
			JSONObject ent = json.getJSONObject(i);
			
			Msg_Log _ml = new Msg_Log(msgTable, logTable, mainTable, mainLogTable);
			_ml.setMod_id(mod_id);
			_ml.setMsgid(ent.getString("msgid"));

//			_ml.setLog_date_table(logTable+"_"+ent.getString("reg_dt").substring(0,7).replace("-",""));
			_ml.setLog_date_table(logTable+"_"+currentMonth);

			String code = "0000";

			if(ent.getString("message_type").equalsIgnoreCase("AT")){

				code = _kaoCode.getOrDefault(ent.getString("code"),"E999");

				if(ent.getString("code").equals("0000")){
					msg_list.add(ent.getString("msgid"));
					continue;
				}
				_ml.setReal_send_date(ent.getString("res_dt"));
				_ml.setResult_msg(ent.getString("message"));
				_ml.setMsg_type("K1");
			}else{
				code = _msgCode.getOrDefault(ent.getString("code"),"8011");
				_ml.setReal_send_date(ent.getString("remark2"));
				_ml.setResult_msg(ent.getString("message"));
				if(ent.getString("sms_kind").equalsIgnoreCase("S")){
					_ml.setMsg_type("M1");
				}else if(ent.getString("sms_kind").equalsIgnoreCase("L")){
					_ml.setMsg_type("M2");
				}
			}

			_ml.setResult_code(code);

			try {
				requestService.update_msg_log(_ml);
			}catch (Exception e) {
				log.info("결과 처리 오류 [ " + _ml.getMsgid() + " ] - " + e.toString());
			}
		}

		if(msg_list.size() > 0) {
			String msg_id = String.join(",", msg_list);

			Msg_Log result = new Msg_Log(msgTable, logTable, mainTable, mainLogTable);
			result.setLog_date_table(logTable+"_"+currentMonth);
			result.setMsgid(msg_id);
			result.setMod_id(mod_id);
			result.setResult_code("0000");

			try{
				requestService.update_msg_log_success(result);
			}catch (Exception e){
				log.info("결과 처리 오류 / {} / {}",msg_id, e.toString());
			}
		}
		log.info("결과 수신 완료 : " + json.length() + " 건");		
		procCnt--;
		
	}

	static public void setIsStart(boolean _flag) {
		log.info(role + " Result Request is  change : " + _flag);
		isStart = _flag;
	}

}
