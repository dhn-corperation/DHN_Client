package com.dhn.client.controller;

import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.MSGService;
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
public class LMSSendRequest implements ApplicationListener<ContextRefreshedEvent> {

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
    private ApplicationContext appContext;

    @Autowired
    private MSGService msgService;


    @Autowired
    ScheduledAnnotationBeanPostProcessor posts;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
        param.setLms_use(appContext.getEnvironment().getProperty("dhnclient.lms_use"));
        param.setMsg_type("L");

        dhnServer = appContext.getEnvironment().getProperty("dhnclient.dhn_kakao_server");
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");
        crypto = appContext.getEnvironment().getProperty("dhnclient.crypto");

        if (param.getLms_use() != null && param.getLms_use().equalsIgnoreCase("Y")) {
            log.info("LMS 초기화 완료");
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

            try{
                int cnt = requestService.selectMSGRequestCount(param);

                if(cnt > 0){
                    requestService.updateMSGStatus(param);

                    List<RequestBean> _list = requestService.selectMSGRequests(param);

                    if(!crypto.isEmpty() && !crypto.equals("")){
                        for (RequestBean requestBean : _list) {
                            requestBean = msgService.encryption(requestBean, crypto);
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
                        Map<String, String> res = om.readValue(response.getBody().toString(), Map.class);
                        log.info(res.toString());
                        if (response.getStatusCode() == HttpStatus.OK) {
                            requestService.updateMSGSendComplete(param);
                            log.info("LMS 메세지 전송 완료(" + response.getStatusCode() + ") : "+ _list.size() + " 건");
                        } else {
                            log.error("LMS 메세지 전송 오류(Http ERR) : " + res.get("userid") + " / " + res.get("message"));
                            requestService.updateMSGSendInit(param);
                        }
                    } catch (Exception e) {
                        log.error("LMS 메세지 전송 오류(Response) : " + e.toString());
                        requestService.updateMSGSendInit(param);
                    }


                }
            }catch (Exception e){
                log.error("LMS 메세지 전송 오류(Send) : " + e.toString());
            }

            isProc = false;
        }
    }

}
