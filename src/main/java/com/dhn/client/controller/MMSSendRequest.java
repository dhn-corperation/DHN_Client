package com.dhn.client.controller;


import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import com.dhn.client.bean.RequestBean;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MMSSendRequest implements ApplicationListener<ContextRefreshedEvent>{

	public static boolean isStart = false;
	private boolean isProc = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	
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
		param.setMsg_type("6");

		dhnServer = "http://" + appContext.getEnvironment().getProperty("dhnclient.server") + "/";
		userid = appContext.getEnvironment().getProperty("dhnclient.userid");
		
		log.info("초기화 완료 됨. - " + param.getDBType() );
		
		isStart = true;
	}
	
	@Scheduled(fixedDelay = 1000)
	private void SendProcess() {
		if(isStart && !isProc) {
			isProc = true;
			
			try {
				
				int cnt = requestService.selectMMSReqeustCount(param);
				
				if(cnt > 0) {

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
					LocalDateTime now = LocalDateTime.now();
					String group_no = now.format(formatter);

					param.setGroup_no(group_no);

					requestService.updateMMSGroupNo(param);
					
					List<RequestBean> _list = requestService.selectMMSRequests(param);
					
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
												
						if(response.getStatusCode() == HttpStatus.OK)
						{
							requestService.updateSMSSendComplete(param);
							log.info("메세지 전송 완료 : " + group_no + " / " + _list.size() + " 건");
						} else {
							Map<String, String> res = om.readValue(response.getBody().toString(), Map.class);
							log.info("메세지 전송오류 : " + res.get("message"));
							requestService.updateSMSSendInit(param);
						}
					} catch(Exception ex) {
						log.info("메세지 전송 오류 : " + ex.toString());

						requestService.updateSMSSendInit(param);
					}
					
				}
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("MMS Send Error : " + e.toString());
			}
			
			isProc = false;
		}
	}
}

