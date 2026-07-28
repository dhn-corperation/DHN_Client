package com.dhn.client.controller;

import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.dhn.client.service.SMSService;
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
public class TranMsgSendRequest  implements ApplicationListener<ContextRefreshedEvent> {

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
    private ScheduledAnnotationBeanPostProcessor posts;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.tran_msg_table"));
        param.setSms_use(appContext.getEnvironment().getProperty("dhnclient.sms_use"));

        dhnServer = appContext.getEnvironment().getProperty("dhnclient.server");
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");

        if (param.getSms_use() != null && param.getSms_use().equalsIgnoreCase("Y")) {
            isStart = true;
            log.info("Tran Msg 초기화 완료");
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
                String group_no = "TR" + now.format(formatter);

                if (!group_no.equals(preGroupNo)) {
                    try {
                        int cnt = requestService.selectTranRequestCount(param);

                        if (cnt > 0) {
                            log.info("Tran Msg 발송 데이터 처리 시작");
                            param.setGroup_no(group_no);

                            requestService.updateTranGroupNo(param);

                            List<RequestBean> _list = requestService.selectTranRequests(param);

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
                                    requestService.updateTranSendComplete(param);
                                    log.info("Tran Msg 메세지 전송 완료(" + response.getStatusCode() + ") : " + group_no + " / " + _list.size() + " 건");
                                } else {
//                                Map<String, String> res = om.readValue(response.getBody().toString(), Map.class);
                                    log.error("Tran Msg 메세지 전송 오류(Http ERR) : " + res.get("userid") + " / " + res.get("message"));
                                    requestService.updateTranSendInit(param);
                                }
                            } catch (Exception e) {
                                log.error("Tran Msg 메세지 전송 오류(Response) : " + e.toString());
                                requestService.updateTranSendInit(param);
                            }

                        }

                    } catch (Exception e) {
                        log.error("Tran Msg Send Error : " + e.toString());
                    } finally {
                        preGroupNo = group_no;
                    }
                }
            }catch (Exception e) {
                log.error("Tran Msg  Send Process Outer Error : {}", e.toString());
            } finally {
                isProc = false;
            }

        }
    }
}
