package com.dhn.client.service;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class SendService {

    @Autowired
    private RequestService requestService;

    @Autowired
    private KAOService kaoService;

    private final String dhnServer;
    private final String userid;

    public SendService(ApplicationContext appContext) {
        this.dhnServer = "http://" + appContext.getEnvironment().getProperty("dhnclient.server") + "/";
        this.userid = appContext.getEnvironment().getProperty("dhnclient.userid");
    }

    private AtomicInteger activeKAOThreads = new AtomicInteger(0);
    private AtomicInteger activeSMSThreads = new AtomicInteger(0);
    private AtomicInteger activeLMSThreads = new AtomicInteger(0);
    private AtomicInteger activeMMSThreads = new AtomicInteger(0);
    public static final int MAX_THREADS = 5; // 최대 쓰레드 수

    public int getActiveKAOThreads() {
        return activeKAOThreads.get();
    }

    public int getActiveSMSThreads() {
        return activeSMSThreads.get();
    }

    public int getActiveLMSThreads() {
        return activeLMSThreads.get();
    }

    public int getActiveMMSThreads() {
        return activeMMSThreads.get();
    }

    @Async("kaoTaskExecutor")
    public void KAOSendAsync(List<KAORequestBean> _list, SQLParameter paramCopy, String group_no){
        if (activeKAOThreads.incrementAndGet() <= MAX_THREADS) {
            try{
                List<String> json_err_msgid = new ArrayList<>();
                List<KAORequestBean> removeData = new ArrayList<>();
                for (KAORequestBean kaoRequestBean : _list) {
                    if(kaoRequestBean.getButton()!=null && !kaoRequestBean.getButton().isEmpty()){
                        kaoService.Btn_form(kaoRequestBean);
                    }

                    StringWriter valSw = new StringWriter();
                    ObjectMapper valOm = new ObjectMapper();
                    valOm.writeValue(valSw, kaoRequestBean);
                    String jsonString = valSw.toString();

                    KAORequestBean newkao = valOm.readValue(jsonString, KAORequestBean.class);

                    boolean isEqual = kaoRequestBean.equals(newkao);

                    if (!isEqual) {
                        log.info("JSON 변환 작업에 이상이 있는 데이터 입니다. / 메세지 아이디 : "+kaoRequestBean.getMsgid());
                        json_err_msgid.add(kaoRequestBean.getMsgid());
                        removeData.add(kaoRequestBean);
                    }
                }

                _list.removeAll(removeData);

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
                    if (response.getStatusCode() == HttpStatus.OK) {
                        requestService.updateKAOSendComplete(paramCopy);
                        log.info("KAO 메세지 전송 완료 : " + group_no + " / " + _list.size() + " 건");
                    } else if(response.getStatusCode() == HttpStatus.NOT_FOUND){
                        requestService.updateKAOSendInit(paramCopy);
                        log.info("({}) KAO 메세지 전송오류 : {}",res.get("userid"), res.get("message"));
                    } else {
                        log.info("({}) KAO 메세지 전송오류 : {}",res.get("userid"), res.get("message"));
                        requestService.updateKAOSendInit(paramCopy);
                    }
                } catch (Exception e) {
                    log.info("KAO 메세지 전송 오류 : " + e.toString());
                    requestService.updateKAOSendInit(paramCopy);
                }

                Thread.sleep(4000);

                if(!json_err_msgid.isEmpty()){
                    requestService.jsonErrMessage(paramCopy,json_err_msgid);
                }
            }catch (Exception e){
                log.error("KAO 비동기 작업 처리 중 오류 발생: " + e.toString());
            }finally {
                activeKAOThreads.decrementAndGet(); // 작업 완료 후 활성화된 쓰레드 수 감소
            }
        }else{
            //log.info("(내부)KAO 작업의 최대 활성화된 쓰레드 수에 도달했습니다.");
            activeKAOThreads.decrementAndGet(); // 실패한 경우에도 감소
        }
    }


    @Async("smsTaskExecutor")
    public void SMSSendAsync(List<RequestBean> _list, SQLParameter paramCopy, String group_no){
        if (activeSMSThreads.incrementAndGet() <= MAX_THREADS) {
            try{
                List<String> json_err_msgid = new ArrayList<>();
                List<RequestBean> removeData = new ArrayList<>();
                for (RequestBean requestBean : _list) {

                    StringWriter valSw = new StringWriter();
                    ObjectMapper valOm = new ObjectMapper();
                    valOm.writeValue(valSw, requestBean);
                    String jsonString = valSw.toString();

                    RequestBean newsms = valOm.readValue(jsonString, RequestBean.class);

                    boolean isEqual = requestBean.equals(newsms);

                    if (!isEqual) {
                        log.info("JSON 변환 작업에 이상이 있는 데이터 입니다. / 메세지 아이디 : "+requestBean.getMsgid());
                        json_err_msgid.add(requestBean.getMsgid());
                        removeData.add(requestBean);
                    }
                }

                _list.removeAll(removeData);

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
                    if(response.getStatusCode() ==  HttpStatus.OK)
                    {
                        requestService.updateSMSSendComplete(paramCopy);
                        log.info("SMS 메세지 전송 완료 : " + group_no + " / " + _list.size() + " 건");
                    } else if(response.getStatusCode() == HttpStatus.NOT_FOUND){
                        requestService.updateKAOSendInit(paramCopy);
                        log.info("({}) SMS 메세지 전송오류 : {}",res.get("userid"), res.get("message"));
                    } else {
                        log.info("({}) SMS 메세지 전송오류 : {}",res.get("userid"), res.get("message"));
                        requestService.updateSMSSendInit(paramCopy);
                    }
                }catch (Exception e) {
                    log.info("SMS 메세지 전송 오류 : " + e.toString());

                    requestService.updateSMSSendInit(paramCopy);
                }

                Thread.sleep(4000);
                if(!json_err_msgid.isEmpty()){
                    requestService.jsonErrMessage(paramCopy,json_err_msgid);
                }
            }catch (Exception e){
                log.error("SMS 비동기 작업 처리 중 오류 발생: " + e.toString());
            }finally {
                activeSMSThreads.decrementAndGet(); // 작업 완료 후 활성화된 쓰레드 수 감소
            }
        }else{
            //log.info("(내부)KAO 작업의 최대 활성화된 쓰레드 수에 도달했습니다.");
            activeSMSThreads.decrementAndGet(); // 실패한 경우에도 감소
        }
    }

    @Async("lmsTaskExecutor")
    public void LMSSendAsync(List<RequestBean> _list, SQLParameter paramCopy, String group_no){
        if (activeLMSThreads.incrementAndGet() <= MAX_THREADS) {
            try{
                List<String> json_err_msgid = new ArrayList<>();
                List<RequestBean> removeData = new ArrayList<>();
                for (RequestBean requestBean : _list) {

                    StringWriter valSw = new StringWriter();
                    ObjectMapper valOm = new ObjectMapper();
                    valOm.writeValue(valSw, requestBean);
                    String jsonString = valSw.toString();

                    RequestBean newlms = valOm.readValue(jsonString, RequestBean.class);

                    boolean isEqual = requestBean.equals(newlms);

                    if (!isEqual) {
                        log.info("JSON 변환 작업에 이상이 있는 데이터 입니다. / 메세지 아이디 : "+requestBean.getMsgid());
                        json_err_msgid.add(requestBean.getMsgid());
                        removeData.add(requestBean);
                    }
                }

                _list.removeAll(removeData);

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
                    if(response.getStatusCode() ==  HttpStatus.OK)
                    {
                        requestService.updateSMSSendComplete(paramCopy);
                        log.info("LMS 메세지 전송 완료 : " + group_no + " / " + _list.size() + " 건");
                    } else if(response.getStatusCode() == HttpStatus.NOT_FOUND){
                        requestService.updateKAOSendInit(paramCopy);
                        log.info("({}) LMS 메세지 전송오류 : {}",res.get("userid"), res.get("message"));
                    } else {
                        log.info("({}) LMS 메세지 전송오류 : {}",res.get("userid"), res.get("message"));
                        requestService.updateSMSSendInit(paramCopy);
                    }
                }catch (Exception e) {
                    log.info("LMS 메세지 전송 오류 : " + e.toString());

                    requestService.updateSMSSendInit(paramCopy);
                }

                Thread.sleep(4000);
                if(!json_err_msgid.isEmpty()){
                    requestService.jsonErrMessage(paramCopy,json_err_msgid);
                }
            }catch (Exception e){
                log.error("LMS 비동기 작업 처리 중 오류 발생: " + e.toString());
            }finally {
                activeLMSThreads.decrementAndGet(); // 작업 완료 후 활성화된 쓰레드 수 감소
            }
        }else{
            //log.info("(내부)KAO 작업의 최대 활성화된 쓰레드 수에 도달했습니다.");
            activeLMSThreads.decrementAndGet(); // 실패한 경우에도 감소
        }
    }
}
