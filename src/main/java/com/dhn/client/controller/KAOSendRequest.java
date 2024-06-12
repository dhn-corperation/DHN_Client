package com.dhn.client.controller;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.KAOService;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KAOSendRequest implements ApplicationListener<ContextRefreshedEvent> {

	public static boolean isStart = false;
	private boolean isProc = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	private String preGroupNo = "";

    @Autowired
	private RequestService requestService;

	@Autowired
	private ApplicationContext appContext;

	@Autowired
	ScheduledAnnotationBeanPostProcessor posts;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
		param.setKakao(appContext.getEnvironment().getProperty("dhnclient.kakao"));
		param.setMsg_type("A");

		if (param.getKakao() != null && param.getKakao().toUpperCase().equals("Y")) {
			log.info("KAO 초기화 완료");
			isStart = true;
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
			String group_no = now.format(formatter);
			
			if(!group_no.equals(preGroupNo)) {
				
				try {
					int cnt = requestService.selectKAORequestCount(param);
					
					if(cnt > 0) {

						param.setGroup_no(group_no);

						requestService.updateKAOGroupNo(param);

						List<KAORequestBean> _list = requestService.selectKAORequests(param);

						StringWriter sw = new StringWriter();
						ObjectMapper om = new ObjectMapper();
						om.writeValue(sw, _list);

						HttpHeaders header = new HttpHeaders();

						header.setContentType(MediaType.APPLICATION_JSON);
						header.set("userid", userid);

						RestTemplate rt = new RestTemplate();
						HttpEntity<String> entity = new HttpEntity<String>(sw.toString(), header);

						try {
							ResponseEntity<String> response = rt.postForEntity(dhnServer + "req", entity, String.class);

							if (response.getStatusCode() == HttpStatus.OK) {
								requestService.updateKAOSendComplete(param);
								log.info("KAO 메세지 전송 완료 : " + group_no + " / " + _list.size() + " 건");
							} else {
								Map<String, String> res = om.readValue(response.getBody().toString(), Map.class);
								log.info("KAO 메세지 전송오류 : " + res.get("message"));
								requestService.updateKAOSendInit(param);
							}
						} catch (Exception e) {
							log.info("KAO 메세지 전송 오류 : " + e.toString());
							requestService.updateKAOSendInit(param);
						}

					}
					
				}catch (Exception e) {
					log.error("KAO Send Error : " + e.toString());
				}
				
				preGroupNo = group_no;
			}
			
			
			isProc = false;
		}
	}

}
