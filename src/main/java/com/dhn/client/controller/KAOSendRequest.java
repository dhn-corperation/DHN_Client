package com.dhn.client.controller;


import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class KAOSendRequest implements ApplicationListener<ContextRefreshedEvent>{

	public static boolean isStart = false;
	public static boolean isProc = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	private String preGroupNo = "";
	private String dual;
	private static String role;
	
	private static final Logger log = LogManager.getRootLogger();
	
	@Autowired
	private RequestService reqService;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Autowired
	ScheduledAnnotationBeanPostProcessor posts;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// TODO Auto-generated method stub
		param.setMsg_table( appContext.getEnvironment().getProperty("dhnclient.msg_table") );
		param.setKakao( appContext.getEnvironment().getProperty("dhnclient.kakao") );
		param.setMsg_type("K");
		param.setDatabase(appContext.getEnvironment().getProperty("dhnclient.database"));
		dual =  appContext.getEnvironment().getProperty("dhnclient.dual");
		role = appContext.getEnvironment().getProperty("dhnclient.role");

		dhnServer = "http://" + appContext.getEnvironment().getProperty("dhnclient.server") + "/";
		userid = appContext.getEnvironment().getProperty("dhnclient.userid");
		
		String send_msg_limit =  appContext.getEnvironment().getProperty("dhnclient.send_msg_limit");
		if(send_msg_limit != null) {
			param.setSend_msg_limit(send_msg_limit);
		} else {
			param.setSend_msg_limit("1000");
		}
		
		//log.info("초기화 완료 됨. - " + param.getKakao() );
		if(param.getKakao() != null && param.getKakao().toUpperCase().equals("Y")) {
			if(dual != null && dual.toUpperCase().equals("Y")) {
				/*if(role != null && role.toUpperCase().equals("MASTER") ) {
					isStart = true;
				}*/
			} else {
				isStart = true;	
			}
			
		} else {
			posts.postProcessBeforeDestruction(this, null);
		}
	}
	
	@Scheduled(fixedDelay = 100)
	private void SendProcess() {
		if(isStart && !isProc) {
			isProc = true;
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			LocalDateTime now = LocalDateTime.now();
			String group_no = now.format(formatter);
			
			if(!group_no.equals(preGroupNo))
			{			
				try {
					
					int cnt = reqService.selectKAOReqeustCount(param);
					//log.info("Kakao Count : " + cnt);
					if(cnt > 0) {
	
						param.setGroup_no(group_no);
						
						reqService.updateKAOGroupNo(param);
						
						List<KAORequestBean> _list = reqService.selectKAORequests(param);
						
						StringWriter sw = new StringWriter();
						ObjectMapper om = new ObjectMapper();
						om.writeValue(sw, _list);
						
						//log.info(sw.toString());
						
						HttpHeaders header = new HttpHeaders();
						
						header.setContentType(MediaType.APPLICATION_JSON);
						header.set("userid", userid);
						
						RestTemplate rt = new RestTemplate();
						HttpEntity<String> entity = new HttpEntity<String>(sw.toString(), header);
						
						try {
							ResponseEntity<String> response = rt.postForEntity(dhnServer + "req", entity, String.class);
							//log.info(response.getStatusCode() + " / " + response.getBody());
													
							if(response.getStatusCode() ==  HttpStatus.OK)
							{
								reqService.updateKAOSendComplete(param);
								//log.info("메세지 전송 완료 : " + group_no + " / " + _list.size() + " 건");
								_list.forEach(msg -> log.info("[ " + msg.getMsgid() + " ] 건 알림톡 송신 완료"));
							} else {
								Map<String, String> res = om.readValue(response.getBody().toString(), Map.class);
								log.info("메세지 전송오류 : " + res.get("message"));
								reqService.updateKAOSendInit(param);
							}
						} catch(Exception ex) {
							log.info("메세지 전송 오류 : " + ex.toString());
							
							reqService.updateKAOSendInit(param);
						}
						
					}
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					log.error("SMS Send Error : " + e.toString());
				}
				preGroupNo = group_no;
			}
			
			isProc = false;
		}
	}
	
	static public void setIsStart(boolean _flag) {
		log.info(role + " KAKAO Sender Request is change : " + _flag);
		isStart = _flag;
	}
}

