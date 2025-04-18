package com.dhn.client.controller;

import com.dhn.client.bean.MessageRequestBean;
import com.dhn.client.bean.SQLParameter;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class ImmediateMSGSendRequest implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isProc = false;
    private SQLParameter param = new SQLParameter();
    private String dhnServer;
    private String userid;
    private String crypto = "";
    private String preGroupNo = "";

    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
        param.setProfile_key(appContext.getEnvironment().getProperty("dhnclient.kakao_profile_key"));

        dhnServer = appContext.getEnvironment().getProperty("dhnclient.dhn_kakao_server");
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");
        crypto = appContext.getEnvironment().getProperty("dhnclient.crypto");

        param.setMsg_type("L");
        param.setPriority("1");

        log.info("Immediate MSG L 초기화 완료");
        isStart = true;

    }

    @Scheduled(fixedDelay = 100)
    private void SendProcess() {
        if(isStart && !isProc) {
            isProc = true;

            ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) executorService;
            int activeThreads = poolExecutor.getActiveCount();

            if(activeThreads < 1){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                LocalDateTime now = LocalDateTime.now();
                String group_no = "L" + now.format(formatter);

                if(!group_no.equals(preGroupNo)){
                    try{
                        int cnt = requestService.selectMessageRequestCount(param);

                        if(cnt > 0){
                            param.setGroup_no(group_no);
                            requestService.updateGroupNo(param);

                            executorService.submit(() -> APIProcess(group_no));

                        }
                    }catch (Exception e){
                        log.error("Immediate MSG L 메세지 전송 오류(Send) : " + e.toString());
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
            sendParam.setMsg_table(param.getMsg_table());
            sendParam.setProfile_key(param.getProfile_key());
            sendParam.setMsg_type(param.getMsg_type());

            List<MessageRequestBean> _list = requestService.selectMsgMessageRequests(sendParam);

            StringWriter sw = new StringWriter();
            ObjectMapper om = new ObjectMapper();
            om.writeValue(sw, _list);

//            log.info(sw.toString());

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
                    requestService.updateMessageComplete(sendParam);
                    log.info("Immediate MSG L 메세지 전송 완료(" + response.getStatusCode() + ") : "+ _list.size() + " 건");
                } else {
                    log.error("Immediate MSG L 메세지 전송 오류(Http ERR) : " + res.get("userid") + " / " + res.get("message"));
                    requestService.updateMessageInit(sendParam);
                }
            } catch (Exception e) {
                log.error("Immediate MSG L 메세지 전송 오류(Response) : " + e.toString());
                requestService.updateMessageInit(sendParam);
            }

        }catch (Exception e){
            log.error("Immediate MSG L 메세지 전송 오류(Send) : " + e.toString());
        }
    }
}