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
import org.springframework.web.client.RestTemplate;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class KAOMMSMSGSendRequest implements ApplicationListener<ContextRefreshedEvent> {
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
    private KAOService kaoService;


    @Autowired
    ScheduledAnnotationBeanPostProcessor posts;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.mms_msg_table"));
        param.setKakao_use(appContext.getEnvironment().getProperty("dhnclient.kakao_use"));
        param.setProfile_key(appContext.getEnvironment().getProperty("dhnclient.profile_key"));
        param.setMsg_type("T");

        dhnServer = appContext.getEnvironment().getProperty("dhnclient.server");
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");

        if (param.getKakao_use() != null && param.getKakao_use().equalsIgnoreCase("Y")) {
            log.info("KAO 초기화 완료");
            isStart = true;
        } else {
            posts.postProcessBeforeDestruction(this, null);
        }

    }

    @Scheduled(fixedDelay = 500)
    private void SendProcess() {
        if (isStart && !isProc) {
            isProc = true;

            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
                LocalDateTime now = LocalDateTime.now();
                String group_no = now.format(formatter);

                if (!group_no.equals(preGroupNo)) {

                    try {
                        int cnt = requestService.selectKAOMMSMSGRequestCount(param);

                        if (cnt > 0) {
                            log.info("KAO 발송 데이터 처리 시작");
                            param.setGroup_no(group_no);

                            requestService.updateKAOMMSMSGGroupNo(param);

                            List<KAORequestBean> _list = requestService.selectKAOMMSMSGRequests(param);

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
                                    requestService.updateKAOMMSMSGSendComplete(param);
                                    log.info("KAO 메세지 전송 완료 : " + group_no + " / " + _list.size() + " 건");
                                } else {
//                                Map<String, String> res = om.readValue(response.getBody().toString(), Map.class);
                                    log.error("KAO 메세지 전송오류 : " + res.get("message"));
                                    requestService.updateKAOMMSMSGSendInit(param);
                                }
                            } catch (Exception e) {
                                log.error("KAO 메세지 전송 오류 : " + e.toString());
                                requestService.updateKAOMMSMSGSendInit(param);
                            }

                        }

                    } catch (Exception e) {
                        log.error("KAO Send Error : " + e.toString());
                    } finally {
                        preGroupNo = group_no;
                    }
                }
            }catch (Exception e) {
                log.error("KAO Send Process Outer Error : {}", e.toString());
            } finally {
                isProc = false;
            }
        }
    }
}
