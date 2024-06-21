package com.dhn.client.controller;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.KAOService;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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
	private KAOService kaoService;

	@Autowired
	private ApplicationContext appContext;

	@Autowired
	ScheduledAnnotationBeanPostProcessor posts;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
		param.setKakao(appContext.getEnvironment().getProperty("dhnclient.kakao"));
		param.setMsg_type("A");

		dhnServer = "http://" + appContext.getEnvironment().getProperty("dhnclient.server") + "/";
		userid = appContext.getEnvironment().getProperty("dhnclient.userid");

		if (param.getKakao() != null && param.getKakao().equalsIgnoreCase("Y")) {
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
			String group_no = "1" + now.format(formatter);
			
			if(!group_no.equals(preGroupNo)) {
				
				try {
					int cnt = requestService.selectKAORequestCount(param);
					
					if(cnt > 0) {

						param.setGroup_no(group_no);

						requestService.updateKAOGroupNo(param);

						List<KAORequestBean> _list = requestService.selectKAORequests(param);

						for (KAORequestBean kaoRequestBean : _list) {
							if(kaoRequestBean.getButton() != null && !kaoRequestBean.getButton().isEmpty()){
								kaoRequestBean = kaoService.Btn_form(kaoRequestBean);
							}
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
							ResponseEntity<String> response = rt.postForEntity(dhnServer + "req", entity, String.class);
							param.setRescode(String.valueOf(response.getStatusCodeValue()));

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
