package com.dhn.client.controller;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.dhn.client.bean.ButtonJsonBean;
import com.dhn.client.bean.Msg_Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
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
	private int senderMaxLen = 20;
	private int receiverMaxLen = 50;
	private String dual;
	private String dbug = "N";
	private static String role;
	private String msgTable = "";
	private String logTable = "";
	private String mainTable = "";
	private String mainLogTable = "";
	private String mod_id = "";

	private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

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
		param.setFibp_profile_key(appContext.getEnvironment().getProperty("dhnclient.fibp_profile_key"));
		param.setInsure_profile_key(appContext.getEnvironment().getProperty("dhnclient.insure_profile_key"));
		param.setNps_profile_key(appContext.getEnvironment().getProperty("dhnclient.nps_profile_key"));
		param.setMod_id((appContext.getEnvironment().getProperty("dhnclient.mod_id")));
		param.setMsg_type("K1");

		dhnServer = appContext.getEnvironment().getProperty("dhnclient.dhn_kakao_server");
		userid = appContext.getEnvironment().getProperty("dhnclient.userid");
		dual = appContext.getEnvironment().getProperty("dhnclient.dual");
		role = appContext.getEnvironment().getProperty("dhnclient.role");
		dbug = appContext.getEnvironment().getProperty("dhnclient.dbug","N");

		msgTable = appContext.getEnvironment().getProperty("dhnclient.msg_table");
		logTable = appContext.getEnvironment().getProperty("dhnclient.log_table");
		mainTable = appContext.getEnvironment().getProperty("dhnclient.main_table");
		mainLogTable = appContext.getEnvironment().getProperty("dhnclient.main_log_table");
		mod_id = appContext.getEnvironment().getProperty("dhnclient.mod_id");

		if (param.getKakao_use() != null && param.getKakao_use().equalsIgnoreCase("Y")) {
			if(dual != null && dual.equalsIgnoreCase("Y")){

			} else {
				isStart = true;
				log.info("KAO 초기화 완료");
			}
		} else {
			posts.postProcessBeforeDestruction(this, null);
		}

		if(dbug.equalsIgnoreCase("Y")){
			log.info("KAO setting value : " + param.toString());
		}
	}


	@Scheduled(fixedDelay = 100)
	private void SendProcess() {
		if(isStart && !isProc) {
			isProc = true;

			ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) executorService;
			int activeThreads = poolExecutor.getActiveCount();

			if(activeThreads < 5){
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
				LocalDateTime now = LocalDateTime.now();
				String group_no = "K" + now.format(formatter);

				if(!group_no.equals(preGroupNo)){
					try{
						int cnt = requestService.selectKAORequestCount(param);

						if(cnt > 0){
							param.setGroup_no(group_no);
							requestService.kaoGroupUpdate(param);

							executorService.submit(() -> APIProcess(group_no));

						}
					}catch (Exception e){
						log.error("KAO 메세지 전송 오류(Send) : " + e.toString());
					}
					preGroupNo = group_no;
				}
			}

			isProc = false;
		}
	}

	private void APIProcess(String group_no) {
		try{

			SQLParameter sendParam = new SQLParameter();
			sendParam.setGroup_no(group_no);
			sendParam.setMsg_table(msgTable);
			sendParam.setLog_table(logTable);
			sendParam.setMain_table(mainTable);
			sendParam.setMod_id(mod_id);


			List<KAORequestBean> _list = requestService.selectKAORequests(sendParam);
			List<String> msg_list = new ArrayList<String>();
			List<String> phnerr_msgid = new ArrayList<>();
			List<String> syserr_msgid = new ArrayList<>();
			List<String> dateerr_msgid = new ArrayList<>();

			List<KAORequestBean> sendList = new ArrayList<>();

			for (KAORequestBean kaoRequestBean : _list) {

				try{
					if(kaoRequestBean.getDateflag().equalsIgnoreCase("0")){
						dateerr_msgid.add(kaoRequestBean.getMsgid());
						continue;
					}

					if(StringUtils.isBlank(kaoRequestBean.getSyscd()) || StringUtils.isEmpty(kaoRequestBean.getSyscd())){
						syserr_msgid.add(kaoRequestBean.getMsgid());
						continue;
					}

					if(StringUtils.isBlank(kaoRequestBean.getSmssender())
							|| StringUtils.length(kaoRequestBean.getSmssender()) > senderMaxLen
							|| !kaoRequestBean.getSmssender().matches("^[0-9-]+$")){

						phnerr_msgid.add(kaoRequestBean.getMsgid());
						continue;
					}

					if(StringUtils.isBlank(kaoRequestBean.getPhn())
							|| StringUtils.length(kaoRequestBean.getPhn()) > receiverMaxLen
							|| !kaoRequestBean.getPhn().matches("^[0-9-]+$")){

						phnerr_msgid.add(kaoRequestBean.getMsgid());
						continue;
					}

					if(kaoRequestBean.getPhn().startsWith("0")){
						kaoRequestBean.setPhn("82"+kaoRequestBean.getPhn().substring(1));
					}



					if(kaoRequestBean.getBtnname() != null){

						String[] btnname = kaoRequestBean.getBtnname().split("\\|",-1);
						String[] btntype = kaoRequestBean.getBtntype() != null && !kaoRequestBean.getBtntype().isEmpty() ? kaoRequestBean.getBtntype().split("\\|",-1) : new String[btnname.length];

						String[] btnmo = kaoRequestBean.getBtnmo() != null && !kaoRequestBean.getBtnmo().isEmpty() ? kaoRequestBean.getBtnmo().split("\\|",-1) : new String[btnname.length];
						String[] btnpc = kaoRequestBean.getBtnpc() != null && !kaoRequestBean.getBtnpc().isEmpty() ? kaoRequestBean.getBtnpc().split("\\|",-1) : new String[btnname.length];

						if (btntype.length < btnname.length) {
							String[] tempBtntype = new String[btnname.length];
							for (int i = 0; i < tempBtntype.length; i++) {
								tempBtntype[i] = (i < btntype.length) ? btntype[i] : "";
							}
							btntype = tempBtntype;
						}

						if (btnpc.length < btnname.length) {
							String[] tempBtnpc = new String[btnname.length];
							for (int i = 0; i < tempBtnpc.length; i++) {
								tempBtnpc[i] = (i < btnpc.length) ? btnpc[i] : "";
							}
							btnpc = tempBtnpc;
						}

						if (btnmo.length < btnname.length) {
							String[] tempBtnmo = new String[btnname.length];
							for (int i = 0; i < tempBtnmo.length; i++) {
								tempBtnmo[i] = (i < btnmo.length) ? btnmo[i] : "";
							}
							btnmo = tempBtnmo;
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
				}catch (Exception e){
					Msg_Log _ml = new Msg_Log(msgTable, logTable, mainTable, mainLogTable);
					_ml.setMod_id(mod_id);
					_ml.setMsgid(kaoRequestBean.getMsgid());
					_ml.setSource_err_msg(e.getMessage());
					requestService.sourceErrUpdate(_ml);
					log.error("KAO 데이터 제조 오류 발생 : " + e.getMessage());
				}

				msg_list.add(kaoRequestBean.getMsgid());
				sendList.add(kaoRequestBean);
			}

			DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMM");
			String ym = LocalDateTime.now().format(formatter2);

			if(phnerr_msgid.size() > 0){
				String strerrmsg = String.join(",", phnerr_msgid);

				Msg_Log _ml = new Msg_Log(msgTable, logTable, mainTable, mainLogTable);
				_ml.setMod_id(mod_id);
				_ml.setMsgid(strerrmsg);

				_ml.setLog_date_table(logTable+"_"+ym);

				_ml.setResult_code("U010");
				_ml.setResult_msg("번호 체크 오류처리");

				requestService.phnErrUpdateDelete(_ml);
				log.info("KAO {} 건 번호체크 오류", phnerr_msgid.size());
			}

			if(syserr_msgid.size() > 0){
				String syserrmsg = String.join(",", syserr_msgid);

				Msg_Log _ml = new Msg_Log(msgTable, logTable, mainTable, mainLogTable);
				_ml.setMod_id(mod_id);
				_ml.setMsgid(syserrmsg);

				_ml.setLog_date_table(logTable+"_"+ym);

				_ml.setResult_code("U005");
				_ml.setResult_msg("등록되지 않은 시스템코드");

				requestService.phnErrUpdateDelete(_ml);
				log.info("KAO {} 건 미등록 시스템코드", syserr_msgid.size());
			}

			if(dateerr_msgid.size() > 0){
				String syserrmsg = String.join(",", dateerr_msgid);

				Msg_Log _ml = new Msg_Log(msgTable, logTable, mainTable, mainLogTable);
				_ml.setMod_id(mod_id);
				_ml.setMsgid(syserrmsg);

				_ml.setLog_date_table(logTable+"_"+ym);

				_ml.setResult_code("U009");
				_ml.setResult_msg("오늘보다 작은 발송일자 오류처리");

				requestService.phnErrUpdateDelete(_ml);
				log.info("KAO {} 건 지난 발송일자", dateerr_msgid.size());
			}

			if(sendList.size() > 0){
				String strmsg = String.join(",", msg_list);

				sendParam.setMsgid_list(msg_list);
				sendParam.setStrmsgid(strmsg);

				StringWriter sw = new StringWriter();
				ObjectMapper om = new ObjectMapper();
				om.writeValue(sw, sendList);

				if(dbug.equalsIgnoreCase("Y")){
					log.info("KAO data : " + sw.toString());
				}

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
						requestService.updateKAOSendComplete(sendParam);
						log.info("KAO 메세지 전송 완료(" + response.getStatusCode() + ") : "+ sendList.size() + " 건");
					} else {
						log.error("KAO 메세지 전송 오류(Http ERR) : " + res.get("userid") + " / " + res.get("message"));
						requestService.updateKAOSendInit(sendParam);
					}
				} catch (Exception e) {
					log.error("KAO 메세지 전송 오류(Response) : " + e.toString());
					requestService.updateKAOSendInit(sendParam);
				}
			}
		} catch (Exception e){
			log.error("KAO 메세지 전송 오류(Send) : " + e.toString());
		} finally {
			if (executorService.isTerminated()) {
				executorService.shutdown();
				log.info("ExecutorService 종료 완료");
			}
		}
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

	static public void setIsStart(boolean _flag) {
		log.info(role + " KAO Process is change : " + _flag);
		isStart = _flag;
	}
}
