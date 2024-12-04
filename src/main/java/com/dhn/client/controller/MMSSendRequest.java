package com.dhn.client.controller;

import java.io.File;
import java.io.StringWriter;
import java.time.LocalDate;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dhn.client.bean.MMSImageBean;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.dhn.client.service.SMSService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
@Slf4j
public class MMSSendRequest implements ApplicationListener<ContextRefreshedEvent>{
	
	public static boolean isStart = false;
	private boolean isProc = false;
	private boolean isProcMms = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	private String basepath;
	private String preGroupNo = "";
	private String crypto = "";
	
	@Autowired
	private RequestService requestService;
	
	@Autowired
	private SMSService smsService;
	
	@Autowired
	private ApplicationContext appContext;

	@Autowired
	ScheduledAnnotationBeanPostProcessor posts;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		param.setMsg_table( appContext.getEnvironment().getProperty("dhnclient.msg_table") );
		param.setDbtype(appContext.getEnvironment().getProperty("dhnclient.database"));
		param.setMms_use(appContext.getEnvironment().getProperty("dhnclient.mms_use"));
		param.setMsg_type("M");
		

		dhnServer = appContext.getEnvironment().getProperty("dhnclient.server");
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
		if(isStart && !isProc) {
			isProc = true;
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
			LocalDateTime now = LocalDateTime.now();
			String group_no = "M" + now.format(formatter);
			
			if(!group_no.equals(preGroupNo)) {
				
				try {
					
					int cnt = requestService.selectMMSReqeustCount(param);
					
					if(cnt > 0) {
						
						param.setGroup_no(group_no);

						requestService.updateMMSGroupNo(param);
						
						List<RequestBean> _list = requestService.selectMMSRequests(param);
						
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
							ResponseEntity<String> response = rt.postForEntity(dhnServer + "testyyw", entity, String.class);

							if(response.getStatusCode() == HttpStatus.OK)
							{
								requestService.updateSMSSendComplete(param);
								log.info("MMS 메세지 전송 완료 : " + group_no + " / " + _list.size() + " 건");
							} else {
								Map<String, String> res = om.readValue(response.getBody().toString(), Map.class);
								log.info("MMS 메세지 전송오류 : " + res.get("message"));
								requestService.updateSMSSendInit(param);
							}
						} catch(Exception ex) {
							log.info("MMS 메세지 전송 오류 : " + ex.toString());

							requestService.updateSMSSendInit(param);
						}
						
					}
					
					
				}catch (Exception e) {
					log.error("MMS Send Error : " + e.toString());
				}
				preGroupNo = group_no;
			}
			isProc = false;
		}
	}


	@Scheduled(fixedDelay = 100)
	private void GETImageKey() {
		if(isStart && !isProc) {
			isProc = true;

			try {
				List<MMSImageBean> imgList = requestService.selectMMSImage(param);

				if(imgList.size() > 0) {
					for (MMSImageBean mmsImageBean : imgList) {
						/*
						param.setFile1("X");
						param.setFile2("X");
						param.setFile3("X");

						MultipartBody.Builder builder = new MultipartBody.Builder();
						builder.addFormDataPart("userid", userid);
						if(mmsImageBean.getFile1() != null && mmsImageBean.getFile1().length() > 0) {
							File file = new File(basepath + mmsImageBean.getFile1());
							param.setFile1(mmsImageBean.getFile1());
							builder.addFormDataPart("image1", mmsImageBean.getFile1(), RequestBody.create(MultipartBody.FORM,file));
						}
						if(mmsImageBean.getFile2() != null && mmsImageBean.getFile2().length() > 0) {
							File file = new File(basepath + mmsImageBean.getFile2());
							param.setFile2(mmsImageBean.getFile2());
							builder.addFormDataPart("image2", mmsImageBean.getFile2(), RequestBody.create(MultipartBody.FORM, file));
						}
						if(mmsImageBean.getFile3() != null && mmsImageBean.getFile3().length() > 0) {
							File file = new File(basepath + mmsImageBean.getFile3());
							param.setFile3(mmsImageBean.getFile3());
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
								log.info("MMS Image Key : " + res.get("image_group"));
								if(res.get("image_group") != null && res.get("image_group").length() > 0) {
									param.setMms_key(res.get("image group"));
									requestService.updateMMSImageGroup(param);
								}
							}
							response.close();
						} catch (Exception e) {
							log.info("MMS Image Key 등록 오류 : ", e.toString());
						}

						*/

						param.setFile1("X");
						param.setFile2("X");
						param.setFile3("X");

						// 헤더 설정
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.MULTIPART_FORM_DATA);
						headers.set("userid", userid);

						// MultiValueMap을 사용해 파일 데이터 전송 준비
						MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
						body.add("userid", userid);

						MultipartBody.Builder builder = new MultipartBody.Builder();
						builder.addFormDataPart("userid", userid);
						if(mmsImageBean.getFile1() != null && mmsImageBean.getFile1().length() > 0) {
							File file = new File(basepath + mmsImageBean.getFile1());
							param.setFile1(mmsImageBean.getFile1());
							body.add("image1", new org.springframework.core.io.FileSystemResource(file));
						}
						if(mmsImageBean.getFile2() != null && mmsImageBean.getFile2().length() > 0) {
							File file = new File(basepath + mmsImageBean.getFile2());
							param.setFile2(mmsImageBean.getFile2());
							body.add("image2", new org.springframework.core.io.FileSystemResource(file));
						}
						if(mmsImageBean.getFile3() != null && mmsImageBean.getFile3().length() > 0) {
							File file = new File(basepath + mmsImageBean.getFile3());
							param.setFile3(mmsImageBean.getFile3());
							body.add("image3", new org.springframework.core.io.FileSystemResource(file));
						}

						// HttpEntity 생성
						HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

						RestTemplate restTemplate = new RestTemplate();
						try{
							ResponseEntity<String> response = restTemplate.exchange(dhnServer + "mms/image", HttpMethod.POST, requestEntity, String.class);

							LocalDate now = LocalDate.now();
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
							String currentMonth = now.format(formatter);

							if (response.getStatusCode() == HttpStatus.OK) {
								String responseBody = response.getBody();
								ObjectMapper mapper = new ObjectMapper();
								Map<String, String> res = mapper.readValue(responseBody, Map.class);

								log.info("MMS Image Key : " + res.toString());

								if (res.get("image_group") != null && res.get("image_group").length() > 0) {
									param.setMms_key(res.get("image_group"));
									requestService.updateMMSImageGroup(param);
								} else {
									log.info("MMS 이미지 등록 실패 : " + res.toString());
								}
							} else {
								log.info("MMS 이미지 등록 실패 : " + response.getStatusCode() + " / " + response.getBody());
							}
						}catch (Exception e){
							log.info("MMS Image Key 등록 오류 : ", e.toString());
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
