package com.dhn.client.controller;

import com.dhn.client.bean.MMSImageBean;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.dhn.client.service.SendService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.MediaType;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MMSSendRequest implements ApplicationListener<ContextRefreshedEvent>{
	
	public static boolean isStart = false;
	private boolean isProc = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	private String basepath;
	private String preGroupNo = "";
	
	@Autowired
	private RequestService requestService;
	
	@Autowired
	private ApplicationContext appContext;

	@Autowired
	private SendService sendService;

	@Autowired
	private ScheduledAnnotationBeanPostProcessor posts;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
		param.setImg_table(appContext.getEnvironment().getProperty("dhnclient.img_table"));
		param.setMms_use(appContext.getEnvironment().getProperty("dhnclient.mms_use"));
		param.setMsg_type("M");
		

		dhnServer = "http://" + appContext.getEnvironment().getProperty("dhnclient.server") + "/";
		userid = appContext.getEnvironment().getProperty("dhnclient.userid");
		
		// 풀 경로를 DB에 담는듯.
		basepath = appContext.getEnvironment().getProperty("dhnclient.file_base_path")==null?"":appContext.getEnvironment().getProperty("dhnclient.file_base_path");

		if (param.getMms_use() != null && param.getMms_use().equalsIgnoreCase("Y")) {
			log.info("MMS 초기화 완료");
			isStart = true;
		} else {
			posts.postProcessBeforeDestruction(this, null);
		}
	}
	
	@Scheduled(fixedDelay = 100)
	private void SendProcess() {
		if(isStart && !isProc && sendService.getActiveMMSThreads() < SendService.MAX_THREADS) {
			isProc = true;
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
			LocalDateTime now = LocalDateTime.now();
			String group_no = "4" + now.format(formatter);
			
			if(!group_no.equals(preGroupNo)) {
				
				try {
					
					int cnt = requestService.selectMMSReqeustCount(param);
					
					if(cnt > 0) {
						
						param.setGroup_no(group_no);
						requestService.updateMMSGroupNo(param);
						List<RequestBean> _list = requestService.selectMMSRequests(param);

						SQLParameter paramCopy = param.toBuilder().build();
						sendService.LMSSendAsync(_list, paramCopy, group_no);

					}
				}catch (Exception e) {
					log.error("MMS Send Error : " + e.toString());
				}
				preGroupNo = group_no;
			}
			isProc = false;
		}else if (sendService.getActiveMMSThreads() >= SendService.MAX_THREADS) {
			//log.info("SMS 스케줄러: 최대 활성화된 쓰레드 수에 도달했습니다. 다음 주기에 다시 시도합니다.");
		}
	}
	
	
	@Scheduled(fixedDelay = 100)
	private void GETImageKey() {
		if(isStart && !isProc) {
			isProc = true;
			
			try {

				int cnt = requestService.selectMMSImageCount(param);

				if(cnt > 0){
					List<MMSImageBean> imgList = requestService.selectMMSImage(param);

					for (MMSImageBean mmsImageBean : imgList) {
						param.setFkContent(mmsImageBean.getFkContent());
						param.setMsgid(mmsImageBean.getMsgid());

						MultipartBody.Builder builder = new MultipartBody.Builder();
						builder.addFormDataPart("userid", userid);
						if(mmsImageBean.getFile1() != null && mmsImageBean.getFile1().length() > 0) {
							File file = new File(basepath + mmsImageBean.getFile1());
							builder.addFormDataPart("image1", mmsImageBean.getFile1(), RequestBody.create(MultipartBody.FORM,file));
						}
						if(mmsImageBean.getFile2() != null && mmsImageBean.getFile2().length() > 0) {
							File file = new File(basepath + mmsImageBean.getFile2());
							builder.addFormDataPart("image2", mmsImageBean.getFile2(), RequestBody.create(MultipartBody.FORM, file));
						}
						if(mmsImageBean.getFile3() != null && mmsImageBean.getFile3().length() > 0) {
							File file = new File(basepath + mmsImageBean.getFile3());
							builder.addFormDataPart("image3", mmsImageBean.getFile3(), RequestBody.create(MultipartBody.FORM, file));
						}

						builder.setType(MultipartBody.FORM);

						RequestBody reqbody = builder.build();

						Request request = new Request.Builder()
								.url(dhnServer + "mms/image")
								.post(reqbody)
								.build();

						try {
							OkHttpClient client = new OkHttpClient();
							Response response = client.newCall(request).execute();

							if(response.code() == 200) {
								ObjectMapper mapper = new ObjectMapper();
								Map<String, String> res = mapper.readValue(response.body().string(), Map.class);
								//log.info("MMS Image Key : " + res.get("image group"));
								if(res.get("image group") != null && res.get("image group").length() > 0) {
									param.setMms_key(res.get("image group"));
									requestService.updateMMSImageGroup(param);
								}
							}
							response.close();
						} catch (Exception e) {
							log.error("MMS Image Key 등록 오류 : ", e.toString());
						}

					}

				}
				
			} catch (Exception e) {
				log.error("MMS Image 등록 오류 : " + e.toString());
			}
		}
		isProc = false;
	}
	

}
