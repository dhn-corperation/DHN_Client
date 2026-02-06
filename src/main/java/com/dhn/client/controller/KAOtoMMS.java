package com.dhn.client.controller;


import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.dhn.client.bean.SQLParameter;
import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.KAOtoMMSBean;
import com.dhn.client.bean.Msg_Log;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class KAOtoMMS implements ApplicationListener<ContextRefreshedEvent>{

	public static boolean isStart = false;
	private boolean isProc = false;
	private String kakaot = "";
	private String kakaotl = "";
	private String smst = "";
	private String smstl = "";
	private String lmst = "";
	private String lmstl = "";
	private String preGroupNo = "";
	private KAOtoMMSBean param = new KAOtoMMSBean();
	
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
		kakaot = appContext.getEnvironment().getProperty("dhnclient.kakao_table");
		kakaotl = appContext.getEnvironment().getProperty("dhnclient.kakao_table_log");
		smst = appContext.getEnvironment().getProperty("dhnclient.sms_table");
		smstl = appContext.getEnvironment().getProperty("dhnclient.sms_table_log");
		lmst = appContext.getEnvironment().getProperty("dhnclient.mms_table");
		lmstl = appContext.getEnvironment().getProperty("dhnclient.mms_table_log");
		
		param.setKmsg_table(kakaot);
		param.setKlog_table(kakaotl);
		param.setSmsg_table(smst);
		param.setSlog_table(smstl);
		param.setMmsg_table(lmst);
		param.setMlog_table(lmstl);
		
		isStart = true;
	}
	
	@Scheduled(fixedDelay = 1000)
	private void SendProcess() {
		if(isStart && !isProc) {
			isProc = true;
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			LocalDateTime now = LocalDateTime.now();
			String group_no = now.format(formatter);
			
			if(!group_no.equals(preGroupNo))
			{
				
				try {
					int cnt = reqService.kakao_to_mms_count(param);

					if(cnt > 0) {
						param.setGroup_no(group_no);
						try {
							reqService.kakao_to_mms_group_update(param);
						} catch (Exception e) {
							// TODO: handle exception
						}finally {
							reqService.kakao_to_mms_move(param);
						}
					}					   
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					log.error("알림톡 Table > MMS Table 오류 : " + e.toString());
				}
				preGroupNo = group_no;
			}
			
			isProc = false;
		}
	}
}

