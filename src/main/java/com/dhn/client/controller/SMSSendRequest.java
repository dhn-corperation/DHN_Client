package com.dhn.client.controller;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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

import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.dhn.client.service.SMSService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SMSSendRequest implements ApplicationListener<ContextRefreshedEvent>{
	
	public static boolean isStart = false;
	private boolean isProc = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	private String preGroupNo = "";
	private String crypto = "";
	
	@Autowired
	private RequestService requestService;
	
	@Autowired
	private SMSService smsService;
	
	@Autowired
	private ApplicationContext appContext;

	@Autowired
	private ScheduledAnnotationBeanPostProcessor posts;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
		param.setDbtype(appContext.getEnvironment().getProperty("dhnclient.database"));
		param.setSms_use(appContext.getEnvironment().getProperty("dhnclient.sms_use"));
		param.setMsg_type("S");
		
		dhnServer = appContext.getEnvironment().getProperty("dhnclient.server");
		userid = appContext.getEnvironment().getProperty("dhnclient.userid");

		if (param.getSms_use() != null && param.getSms_use().equalsIgnoreCase("Y")) {
			isStart = true;
			log.info("SMS 초기화 완료");
		} else {
			posts.postProcessBeforeDestruction(this, null);
		}

	}
	
	
	@Scheduled(fixedDelay = 100)
	private void SendProcess() {
		if(isStart && !isProc) {
			isProc = true;
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
			LocalDateTime now = LocalDateTime.now();
			String group_no = "S" + now.format(formatter);
			
			if(!group_no.equals(preGroupNo)) {
				try {
					int cnt = requestService.selectSMSReqeustCount(param);
					
					if(cnt > 0) {
						param.setGroup_no(group_no);
						
						requestService.updateSMSGroupNo(param);
						
						List<RequestBean> _list = requestService.selectSMSRequests(param);
						
						for (RequestBean requestBean : _list) {
							requestBean = smsService.encryption(requestBean,crypto);
						}
						
						StringWriter sw = new StringWriter();
						ObjectMapper om = new ObjectMapper();
						om.writeValue(sw, _list);
						
						HttpHeaders header = new HttpHeaders();
						
						header.setContentType(MediaType.APPLICATION_JSON);
						header.set("userid", userid);
						
						RestTemplate rt = new RestTemplate();
						HttpEntity<String> entity = new HttpEntity<String>(sw.toString(), header);
						
						try {
							//ResponseEntity<String> response = rt.postForEntity(dhnServer + "req", entity, String.class);
							ResponseEntity<String> response = rt.postForEntity(dhnServer + "testyyw", entity, String.class);
							
							if(response.getStatusCode() ==  HttpStatus.OK)
							{
								requestService.updateSMSSendComplete(param);
								log.info("SMS 메세지 전송 완료(" + response.getStatusCode() + ") : " + group_no + " / " + _list.size() + " 건");
							} else {
								Map<String, String> res = om.readValue(response.getBody().toString(), Map.class);
								log.error("SMS 메세지 전송 오류(Http ERR) : " + res.get("userid") + " / " + res.get("message"));
								requestService.updateSMSSendInit(param);
							}
						}catch (Exception e) {
							log.error("SMS 메세지 전송 오류(Response) : " + e.toString());
							requestService.updateSMSSendInit(param);
						}

					}
				
				}catch (Exception e) {
					log.error("SMS Send Error : " + e.toString());
				}
				preGroupNo = group_no;
			}
			isProc = false;
			
		}
	}
	
	

}
