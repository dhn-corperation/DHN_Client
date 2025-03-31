package com.dhn.client.controller;


import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
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
@Slf4j
public class ResultReq implements ApplicationListener<ContextRefreshedEvent>{

	public static boolean isStart = false;
	private boolean isProc = false;
	private String dhnServer;
	private String userid;
	private static int procCnt = 0;
	private String kakaot = "";
	private String kakaotl = "";
	private String tableseq = "";
	
	@Autowired
	private RequestService requestService;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// TODO Auto-generated method stub
		kakaot = appContext.getEnvironment().getProperty("dhnclient.req_table");
		kakaotl = appContext.getEnvironment().getProperty("dhnclient.log_table");
		tableseq = appContext.getEnvironment().getProperty("dhnclient.table_seq");
		 
		dhnServer = "http://" + appContext.getEnvironment().getProperty("dhnclient.server") + "/";
//		dhnServer = "https://" + appContext.getEnvironment().getProperty("dhnclient.server") + "/";
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
					log.info("결과 수신 오류 (response Err) : " + ex.toString());
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
		

		for(int i=0; i<json.length();i++) {
			JSONObject ent = json.getJSONObject(i);
			Msg_Log _ml = new Msg_Log(kakaot, kakaotl);
			String[] mseq = ent.getString("msgid").split("_");
			_ml.setMseq (mseq[0].substring(1));
			_ml.setMsg_type( mseq[0].substring(0,1) );
			_ml.setTable(kakaot);
			_ml.setLog_table(kakaotl);
			if( mseq != null &&  mseq.length >=2 ) {
				if(mseq[1].equalsIgnoreCase("N")){
					_ml.setSecond_flag("N");
				}else{
					_ml.setSecond_flag("Y");
				}
			} else {
				_ml.setSecond_flag("N");

			}

			_ml.setPseq(_ml.getMseq());

			String rscode = "0000";
			_ml.setStat("3");
			
			if(ent.getString("message_type").equalsIgnoreCase("PH"))
			{
				rscode = ent.getString("code");

				_ml.setReport_time(ent.getString("remark2"));

				String telcom = "ETC";

				if(ent.getString("remark1").equalsIgnoreCase("LGT") || ent.getString("remark1").equals("019")){
					telcom = "LGT";
				}else if(ent.getString("remark1").equalsIgnoreCase("SKT") || ent.getString("remark1").equals("011")){
					telcom = "SKT";
				}else if(ent.getString("remark1").equalsIgnoreCase("KTF") || ent.getString("remark1").equalsIgnoreCase("KT") || ent.getString("remark1").equals("016")){
					telcom = "KTF";
				}else{
					telcom = "ETC";
				}

				_ml.setTelecom(telcom);
				_ml.setResult(rscode);
			} else {
				
				_ml.setResult(ent.getString("code"));
				
				_ml.setReport_time(ent.getString("res_dt"));
				_ml.setTelecom("KAK");
				
				rscode = ent.getString("code");
				
				if(!_ml.getResult().equals("0000")) {
					try {
						List<LMSTableBean> _list = requestService.kakao_to_sms_select(_ml);
						for(int ii=0; ii<_list.size();ii++ ) {
							LMSTableBean r = _list.get(ii);
							r.setTable(kakaot);
							//log.info("Mseq > " + r.getMseq() + ", " + _ml.getMseq() );
							if(r.getK_next_type().equals("5")) {
								r.setPseq(r.getMseq());
								r.setMsg_type("3");
								r.setK_next_type("0");
								r.setTable_seq(tableseq);

								byte[] bytes = 	r.getText().getBytes("EUC-KR");
								
								int limit = 1000;
								int length = bytes.length;
								int offset = 0;
								int extIdx = 1;
								while (offset < length) {
									String temp1 = new String(bytes, offset, length-offset, "EUC-KR");
									String temp2 = cutKoreanString(temp1, limit);
									
									int endIdx =offset + temp2.getBytes("EUC-KR").length;
									if(endIdx > length) {
										endIdx = length;
									}
									
									r.setText(temp2.replace("'", "''"));
									r.setExt_col1("" + extIdx);
									requestService.insert_sms(r);
									extIdx++;
									offset = endIdx;
								}
							} else if(r.getK_next_type().equals("8")) {
								r.setPseq(r.getMseq());
								r.setExt_col1("1");
								r.setText(r.getText2().replace("'", "''"));
								r.setMsg_type("3");
								r.setK_next_type("0");
								r.setTable_seq(tableseq);

								requestService.insert_sms(r);
							} else if(r.getK_next_type().equals("4")) {
								r.setPseq(r.getMseq());
								r.setMsg_type("1");
								r.setK_next_type("0");
								r.setTable_seq(tableseq);
								
								byte[] bytes = 	r.getText().getBytes("EUC-KR");
								
								int limit = 90;
								int length = bytes.length;
								int offset = 0;
								int extIdx = 1;
								while (offset < length) {
									
									String temp1 = new String(bytes, offset, length-offset, "EUC-KR");
									String temp2 = cutKoreanString(temp1, limit);
									
									int endIdx =offset + temp2.getBytes("EUC-KR").length;
									if(endIdx > length) {
										endIdx = length;
									}
									
									r.setText(temp2.replace("'", "''"));
									r.setExt_col1("" + extIdx);
									requestService.insert_sms(r);
									extIdx++;
									offset = endIdx;
								}								
							} else if(r.getK_next_type().equals("7")) {
								r.setPseq(r.getMseq());
								r.setExt_col1("1");
								r.setText(r.getText2().replace("'", "''"));
								r.setMsg_type("1");
								r.setK_next_type("0");
								r.setTable_seq(tableseq);

								requestService.insert_sms(r);
							}
							
						}
					} catch (Exception e) {
						log.error("재발송 데이터 Insert 오류 : {}",e.getMessage());
					}

				}
			}

			log.info("Mseq : " + _ml.getMseq() + ", Message Type : " + _ml.getMsg_type() + ", Result : " + _ml.getResult() );
			try {
				requestService.Insert_msg_log(_ml);
			} catch (Exception e) {
				log.info("결과 처리 오류 [ " + _ml.getMseq() + " ] - " + e.toString());
			}
		}
		
		log.info("결과 수신 완료 : " + json.length() + " 건");		
		procCnt--;
	}
	
    public static String cutKoreanString(String input, int maxBytes) {
        if (input == null || input.getBytes().length <= maxBytes) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        int bytes = 0;

        for (char c : input.toCharArray()) {
            try {
				bytes += String.valueOf(c).getBytes("EUC-KR").length;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            if (bytes > maxBytes) {
                break;
            }

            result.append(c);
        }

        return result.toString();
    }
}

