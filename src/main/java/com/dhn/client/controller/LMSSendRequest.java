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
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
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
    private String crypto;

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private MSGService msgService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
        param.setMsg_type("LM");

        dhnServer = "http://" + appContext.getEnvironment().getProperty("dhnclient.server") + "/";
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");

        HttpHeaders cheader = new HttpHeaders();

        cheader.setContentType(MediaType.APPLICATION_JSON);
        cheader.set("userid", userid);

        RestTemplate crt = new RestTemplate();
        HttpEntity<String> centity = new HttpEntity<String>(cheader);

        try {
            ResponseEntity<String> cresponse = crt.exchange( dhnServer + "get_crypto",HttpMethod.GET, centity, String.class );

            if(cresponse.getStatusCode()==HttpStatus.OK) {
                crypto = cresponse.getBody()!=null? cresponse.getBody().toString():"";
                log.info("LMS 초기화 완료");
                isStart = true;
            }else {
                log.info("암호화 컬럼 가져오기 오류 ");
            }

        }catch (HttpClientErrorException e) {
            log.error("crypto 가져오기 오류 : " + e.getStatusCode() + ", " + e.toString());
        }catch (RestClientException e) {
            log.error("기타 오류 : " + dhnServer + ", " + e.toString());
        }
    }

    @Scheduled(fixedDelay = 100)
    private void SendProcess() {
        if(isStart && !isProc) {
            isProc = true;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
            LocalDateTime now = LocalDateTime.now();
            String group_no = "L" + now.format(formatter);

            if(!group_no.equals(preGroupNo)) {
                try {
                    int cnt = requestService.selectLMSReqeustCount(param);

                    if(cnt > 0) {
                        param.setGroup_no(group_no);

                        requestService.updateLMSGroupNo(param);

                        List<RequestBean> _list = requestService.selectLMSRequests(param);

                        for (RequestBean requestBean : _list) {
                            requestBean = msgService.encryption(requestBean,crypto);
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
                            param.setRescode(String.valueOf(response.getStatusCodeValue()));

                            if(response.getStatusCode() ==  HttpStatus.OK)
                            {
                                requestService.updateMSGSendComplete(param);
                                log.info("LMS 메세지 전송 완료 : " + group_no + " / " + _list.size() + " 건");
                            } else {
                                Map<String, String> res = om.readValue(response.getBody().toString(), Map.class);
                                log.info("LMS 메세지 전송오류 : " + res.get("message"));
                                requestService.updateMSGSendInit(param);
                            }
                        }catch (Exception e) {
                            log.info("LMS 메세지 전송 오류 : " + e.toString());

                            requestService.updateMSGSendInit(param);
                        }

                    }

                }catch (Exception e) {
                    log.error("LMS Send Error : " + e.toString());
                }
                preGroupNo = group_no;
            }
            isProc = false;

        }
    }
}
