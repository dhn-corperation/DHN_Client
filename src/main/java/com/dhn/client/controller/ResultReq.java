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
	private String kakaot = "";
	private String kakaotl = "";
	private String smst = "";
	private String smstl = "";
	private String lmst = "";
	private String lmstl = "";
	private String newAgent = "";
	private String sdktable = "";
	
	private static final Logger log = LogManager.getRootLogger();
	
	@Autowired
	private RequestService reqService;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// TODO Auto-generated method stub
		kakaot = appContext.getEnvironment().getProperty("dhnclient.kakao_table");
		kakaotl = appContext.getEnvironment().getProperty("dhnclient.kakao_table_log");
		smst = appContext.getEnvironment().getProperty("dhnclient.sms_table");
		smstl = appContext.getEnvironment().getProperty("dhnclient.sms_table_log");
		lmst = appContext.getEnvironment().getProperty("dhnclient.mms_table");
		lmstl = appContext.getEnvironment().getProperty("dhnclient.mms_table_log");
		sdktable = appContext.getEnvironment().getProperty("dhnclient.sdk_table");
		newAgent = appContext.getEnvironment().getProperty("dhnclient.newagent");
		
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
		
		//log.info("시작 > " + _pc);
		int _procPoint = 0;
		for(int i=0; i<json.length();i++) {
			JSONObject ent = json.getJSONObject(i);
			Msg_Log _ml = new Msg_Log(param.getMsg_table(), param.getKakao());
			_ml.setTran_pr (ent.getString("msgid").substring(1));
			_ml.setMsgType( ent.getString("msgid").substring(0,1) );
			//log.info("MsgType : " + _ml.getMsgType() + " / " + ent.getString("msgid"));
			_ml.setNewagent(newAgent);
			_procPoint = 1;
			_ml.setSdk_table(sdktable);
			
			switch(_ml.getMsgType()) {
			case "S":
				_ml.setMsg_table(smst);
				_ml.setLog_table(smstl);
				break;
			case "L":
				_ml.setMsg_table(lmst);
				_ml.setLog_table(lmstl);
				break;
			case "K":
				_ml.setMsg_table(kakaot);
				_ml.setLog_table(kakaotl);
				break;
			}
			_procPoint = 2;
			String rscode = "06";
			
			if(ent.getString("message_type").toUpperCase().equals("PH")) 
			{
				_procPoint = 3;
				if(!ent.getString("code").equals("0000")) {
					rscode = ent.getString("code").substring(2);
					_ml.setSms_st("5");
				} else {
					_ml.setSms_st("3");
				}
				_procPoint = 4;
				_ml.setCmp_rcv_dttm(ent.getString("remark2"));
				_ml.setRcv_mno_cd(ent.getString("remark1"));
				_ml.setRegdate(ent.getString("reg_dt"));
				_ml.setKmsg_rslt(ent.getString("s_code"));
				if(ent.getString("sms_kind").toUpperCase().equals("S")) {
					_ml.setReplace_type("S");
				} else if(ent.getString("sms_kind").toUpperCase().equals("L")) {
					_ml.setReplace_type("M");
				}
				_procPoint = 5;
				_ml.setSdk_st(_ml.getSms_st());
			} else {
				_procPoint = 6;
				rscode = ent.getString("code");
				if(!ent.getString("code").equals("0000")) {
					//rscode = ent.getString("code").substring(2);
					_ml.setSms_st("5");
				} else {
					_ml.setSms_st("3");
				}
				_procPoint = 7;
				_ml.setKmsg_rslt(ent.getString("code"));
				_ml.setCmp_rcv_dttm(ent.getString("res_dt"));
				_ml.setRegdate(ent.getString("reg_dt"));
				
				_ml.setResultmsg(ent.getString("message"));
				_procPoint = 8;
				if(ent.getString("message_type").toUpperCase().startsWith("A")) 
				{
					_ml.setRcv_mno_cd(ent.getString("message_type").toUpperCase());
				} else {
					_ml.setRcv_mno_cd("FT");
				}
				_procPoint = 9;
				
				_ml.setSdk_st(_ml.getSms_st());
				
				if(ent.getString("code").equals("0000")) {
					if(ent.getString("message_type").toUpperCase().equals("AT")) 
					{
						_ml.setReplace_type("N");
					} else if(ent.getString("message_type").toUpperCase().equals("AI")) {
						_ml.setReplace_type("I");
					}
					
				} else {
					_procPoint = 10;
					LMSTableBean _lmst = new LMSTableBean();
					
					_lmst.setTran_pr(_ml.getTran_pr());
					_procPoint = 11;
					if(ent.getString("phn").startsWith("82")) {
						_lmst.setTran_phone("0" + ent.getString("phn").substring(2));
					} else {
						_lmst.setTran_phone(ent.getString("phn"));
					}
					_procPoint = 12;
					_lmst.setTran_callback(ent.getString("sms_sender") );
					_lmst.setTran_msg(ent.getString("msg_sms").replace("'", "''"));
					_lmst.setTran_subject(ent.getString("sms_lms_tit").replace("'", "''"));
					_procPoint = 13;
					if(ent.getString("remark3").toUpperCase().equals("S")) {
						_ml.setReplace_type("S");
						// SMS 2차 발송을 위한 Insert
						
						_lmst.setMsg_table(smst);
						_ml.setSdk_st("1");
						try {
							reqService.Insert_sms(_lmst);
						} catch (Exception e) {
							log.info("2차 발송 SMS Insert 처리 오류 [ " + _ml.getTran_pr() + " ] - " + e.toString());
						}
					} else if(ent.getString("remark3").toUpperCase().equals("L")) {
						_ml.setReplace_type("M");
						
						// LMS 2차 발송을 위한 Insert 			
						_lmst.setMsg_table(lmst);
						_ml.setSdk_st("1");
						try {
							reqService.Insert_lms(_lmst);
						} catch (Exception e) {
							log.info("2차 발송 LMS Insert 처리 오류 [ " + _ml.getTran_pr() + " ] - " + e.toString());
						}
					}
					_procPoint = 14;
					_ml.setRcv_mno_cd(ent.getString("remark1"));
				}
			}
			_ml.setRslt_val(rscode);
			//if(_ml.getRcv_mno_cd().length()>=2) {
			//	_ml.setRcv_mno_cd(_ml.getRcv_mno_cd().substring(0,1));
			//}
			try {
				reqService.Insert_msg_log(_ml);
				//log.info("결과 수신 완료 : [ " + _ml.getTran_pr() + " ] - " + _ml.getRslt_val() );		
			} catch (Exception e) {
				log.info("결과 처리 오류 [ " + _ml.getTran_pr() + " ] - " + e.toString() + "  " + _procPoint);
			}
		}
		
		log.info("결과 수신 완료 : " + json.length() + " 건"  );		
		procCnt--;
	}
}

