package com.dhn.client.controller;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dhn.client.bean.ButtonJsonBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.SQLParameter;
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
		param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
		param.setMain_table(appContext.getEnvironment().getProperty("dhnclient.main_table"));
		param.setKakao_use(appContext.getEnvironment().getProperty("dhnclient.kakao_use"));
		param.setBdpt_profile_key(appContext.getEnvironment().getProperty("dhnclient.bdpt_profile_key"));
		param.setInsure_profile_key(appContext.getEnvironment().getProperty("dhnclient.insure_profile_key"));
		param.setNps_profile_key(appContext.getEnvironment().getProperty("dhnclient.nps_profile_key"));
		param.setMod_id((appContext.getEnvironment().getProperty("dhnclient.mod_id")));
		param.setMsg_type("K1");

		dhnServer = appContext.getEnvironment().getProperty("dhnclient.dhn_kakao_server");
		userid = appContext.getEnvironment().getProperty("dhnclient.userid");
		dual = appContext.getEnvironment().getProperty("dhnclient.dual");
		role = appContext.getEnvironment().getProperty("dhnclient.role");

		if (param.getKakao_use() != null && param.getKakao_use().equalsIgnoreCase("Y")) {
			if(dual != null && dual.equalsIgnoreCase("Y")){

			} else {
				isStart = true;
				log.info("KAO 초기화 완료");
			}
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

			try{
				int cnt = requestService.selectKAORequestCount(param);

				if(cnt > 0){
					requestService.updateKAOStatus(param);

					List<KAORequestBean> _list = requestService.selectKAORequests(param);
					List<String> msg_list = new ArrayList<String>();

					for (KAORequestBean kaoRequestBean : _list) {
						msg_list.add(kaoRequestBean.getMsgid());

						if(kaoRequestBean.getBtnname() != null){
							String[] btnname = kaoRequestBean.getBtnname().split("\\|");
							String[] btntype = kaoRequestBean.getBtntype().split("\\|");
							String[] btnmo = new String[5];
							String[] btnpc = new String[5];
							if(kaoRequestBean.getBtnmo() != null){
								btnmo = kaoRequestBean.getBtnmo().split("\\|");
							}else{
								btnmo[0] = "";
								btnmo[1] = "";
								btnmo[2] = "";
								btnmo[3] = "";
								btnmo[4] = "";
							}
							if(kaoRequestBean.getBtnpc() != null){
								btnpc = kaoRequestBean.getBtnpc().split("\\|");
							}else{
								btnpc[0] = "";
								btnpc[1] = "";
								btnpc[2] = "";
								btnpc[3] = "";
								btnpc[4] = "";
							}

							if(btnname.length > 0){
								kaoRequestBean.setButton1(Btn_json(btnname[0],btntype[0],btnpc[0],btnmo[0]));
							}
							if(btnname.length > 1){
								kaoRequestBean.setButton2(Btn_json(btnname[1],btntype[1],btnpc[1],btnmo[1]));
							}
							if(btnname.length > 2){
								kaoRequestBean.setButton3(Btn_json(btnname[2],btntype[2],btnpc[2],btnmo[2]));
							}
							if(btnname.length > 3){
								kaoRequestBean.setButton4(Btn_json(btnname[3],btntype[3],btnpc[3],btnmo[3]));
							}
							if(btnname.length > 4){
								kaoRequestBean.setButton5(Btn_json(btnname[4],btntype[4],btnpc[4],btnmo[4]));
							}

						}

					}
					param.setMsgid_list(msg_list);

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
						Map<String, String> res = om.readValue(response.getBody().toString(), Map.class);
						log.info(res.toString());
						if (response.getStatusCode() == HttpStatus.OK) {
							requestService.updateKAOSendComplete(param);
							log.info("KAO 메세지 전송 완료(" + response.getStatusCode() + ") : "+ _list.size() + " 건");
						} else {
							log.error("KAO 메세지 전송 오류(Http ERR) : " + res.get("userid") + " / " + res.get("message"));
							requestService.updateKAOSendInit(param);
						}
					} catch (Exception e) {
						log.error("KAO 메세지 전송 오류(Response) : " + e.toString());
						requestService.updateKAOSendInit(param);
					}

				}
			}catch (Exception e){
				log.error("KAO 메세지 전송 오류(Send) : " + e.toString());
			}
			
			isProc = false;
		}
	}

	static public void setIsStart(boolean _flag) {
		log.info(role + " KAO Process is change : " + _flag);
		isStart = _flag;
	}

	private String Btn_json(String btnname, String btntype, String btnpc, String btnmo) {


		ButtonJsonBean btnjb = new ButtonJsonBean();
		btnjb.setName(btnname);
		btnjb.setType(btntype);
		if(btntype.equalsIgnoreCase("AL")){
			btnjb.setScheme_android(btnmo);
			btnjb.setScheme_ios(btnpc);
		} else {
			btnjb.setUrl_mobile(btnmo);
			btnjb.setUrl_pc(btnpc);
		}

		String jsonString = "";

		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonString = mapper.writeValueAsString(btnjb);
		} catch (JsonProcessingException e) {
			log.error("버튼 제조 에러 : " + e.toString());
		}

		return jsonString;
	}

}
