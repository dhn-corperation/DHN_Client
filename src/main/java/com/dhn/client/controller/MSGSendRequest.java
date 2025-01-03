package com.dhn.client.controller;

import com.dhn.client.bean.MessageRequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.MessageService;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MSGSendRequest implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isProc = false;
    private SQLParameter param = new SQLParameter();
    private String dhnServer;
    private String userid;
    private String crypto = "";

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private MessageService messageService;


    @Autowired
    ScheduledAnnotationBeanPostProcessor posts;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
        param.setProfile_key(appContext.getEnvironment().getProperty("dhnclient.kakao_profile_key"));

        dhnServer = appContext.getEnvironment().getProperty("dhnclient.dhn_kakao_server");
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");
        crypto = appContext.getEnvironment().getProperty("dhnclient.crypto");

        log.info("MSG 초기화 완료");
        isStart = true;

    }

    @Scheduled(fixedDelay = 100)
    private void SendProcess() {
        if(isStart && !isProc) {
            isProc = true;

            try{
                int cnt = requestService.selectMessageRequestCount(param);

                if(cnt > 0){

                    List<MessageRequestBean> _list = requestService.selectMessageRequests(param);

                    log.info("메세지 전송 시작(" + _list.size() + " 건)");

                    for (MessageRequestBean messageRequestBean : _list) {

                        if(messageRequestBean.getMessagetype().equalsIgnoreCase("P")){
                            messageRequestBean.setMessagetype("AP");
                        }else if(messageRequestBean.getMessagetype().equalsIgnoreCase("K")){
                            messageRequestBean.setMessagetype("AT");
                        }else {
                            messageRequestBean.setMessagetype("PH");
                        }


                    }

                    String messageid = _list.stream()
                            .map(MessageRequestBean::getMsgid)
                            .map(msgid -> "'" + msgid + "'")
                            .collect(Collectors.joining(","));

                    param.setMessageid(messageid);

                    if(!crypto.isEmpty() && !crypto.equals("")){
                        for (MessageRequestBean messageRequestBean : _list) {
                            messageRequestBean = messageService.encryption(messageRequestBean, crypto);
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
                            requestService.updateMessageComplete(param);
                            log.info("메세지 전송 완료(" + response.getStatusCode() + ") : "+ _list.size() + " 건");
                        } else {
                            log.error("메세지 전송 오류(Http ERR) : " + res.get("userid") + " / " + res.get("message"));
                            //requestService.updateMessageInit(param);
                        }
                    } catch (Exception e) {
                        log.error("메세지 전송 오류(Response) : " + e.toString());
                        //requestService.updateMessageInit(param);
                    }


                }
            }catch (Exception e){
                log.error("MSG 메세지 전송 오류(Send) : " + e.toString());
            }

            isProc = false;
        }
    }
}
