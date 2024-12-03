package com.dhn.client.controller;

import com.dhn.client.bean.PUSHRequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.PUSHService;
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
public class PUSHSendRequest implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isProc = false;
    private SQLParameter param = new SQLParameter();
    private String pushServer;
    private String userid;
    private String preGroupNo = "";
    private String crypto = "";

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private PUSHService pushService;

    @Autowired
    ScheduledAnnotationBeanPostProcessor posts;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
        param.setPush_use(appContext.getEnvironment().getProperty("dhnclient.push_use"));
        param.setProfile_key(appContext.getEnvironment().getProperty("dhnclient.kakao_profile_key"));
        param.setMsg_type("P");

        pushServer = appContext.getEnvironment().getProperty("dhnclient.dhn_push_server");
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");
        crypto = appContext.getEnvironment().getProperty("dhnclient.crypto");

        if (param.getPush_use() != null && param.getPush_use().equalsIgnoreCase("Y")) {
            log.info("PUSH 초기화 완료");
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
                int cnt = requestService.selectPUSHRequestCount(param);

                if(cnt > 0){
                    requestService.updatePUSHStatus(param);

                    List<PUSHRequestBean> _list = requestService.selectPUSHRequests(param);

                    if(!crypto.isEmpty() && !crypto.equals("")){
                        for (PUSHRequestBean pushRequestBean : _list) {
                            pushRequestBean = pushService.encryption(pushRequestBean, crypto);
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
                        ResponseEntity<String> response = rt.postForEntity(pushServer + "req", entity, String.class);
                        Map<String, String> res = om.readValue(response.getBody().toString(), Map.class);
                        log.info(res.toString());
                        if (response.getStatusCode() == HttpStatus.OK) {
                            requestService.updatePUSHSendComplete(param);
                            log.info("PUSH 메세지 전송 완료(" + response.getStatusCode() + ") : "+ _list.size() + " 건");
                        } else {
                            log.error("PUSH 메세지 전송 오류(Http ERR) : " + res.get("userid") + " / " + res.get("message"));
                            requestService.updatePUSHSendInit(param);
                        }
                    } catch (Exception e) {
                        log.error("PUSH 메세지 전송 오류(Response) : " + e.toString());
                        requestService.updatePUSHSendInit(param);
                    }

                }
            }catch (Exception e){
                log.error("PUSH 메세지 전송 오류(Send) : " + e.toString());
            }

            isProc = false;
        }
    }

}
