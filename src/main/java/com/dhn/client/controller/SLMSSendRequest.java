package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SLMSSendRequest implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isProc = false;
    private SQLParameter param = new SQLParameter();
    private String dhnServer;
    private String userid;
    private String preGroupNo = "";
    private int senderMaxLen = 20;
    private int receiverMaxLen = 50;
    private String dual;
    private String dbug = "N";
    private static String role;
    private String msgTable = "";
    private String logTable = "";
    private String mainTable = "";
    private String mainLogTable = "";
    private String mod_id = "";

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    ScheduledAnnotationBeanPostProcessor posts;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
        param.setMain_table(appContext.getEnvironment().getProperty("dhnclient.main_table"));
        param.setSmslms_use(appContext.getEnvironment().getProperty("dhnclient.smslms_use"));
        param.setMod_id((appContext.getEnvironment().getProperty("dhnclient.mod_id")));
        param.setMsg_type("99");

        dhnServer = appContext.getEnvironment().getProperty("dhnclient.dhn_kakao_server");
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");
        dual = appContext.getEnvironment().getProperty("dhnclient.dual");
        role = appContext.getEnvironment().getProperty("dhnclient.role");
        dbug = appContext.getEnvironment().getProperty("dhnclient.dbug");

        msgTable = appContext.getEnvironment().getProperty("dhnclient.msg_table");
        logTable = appContext.getEnvironment().getProperty("dhnclient.log_table");
        mainTable = appContext.getEnvironment().getProperty("dhnclient.main_table");
        mainLogTable = appContext.getEnvironment().getProperty("dhnclient.main_log_table");
        mod_id = appContext.getEnvironment().getProperty("dhnclient.mod_id");

        if (param.getSmslms_use() != null && param.getSmslms_use().equalsIgnoreCase("Y")) {
            if(dual != null && dual.equalsIgnoreCase("Y")){

            }else{
                isStart = true;
                log.info("SMS/LMS 초기화 완료");
            }
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

            if(dbug.equalsIgnoreCase("Y")){
                log.info("S/LMS setting value : " + param.toString());
            }

            try{
                int cnt = requestService.selectMSGRequestCount(param);

                if(cnt > 0){
                    requestService.updateMSGStatus(param);

                    List<RequestBean> _list = requestService.selectMSGRequests(param);
                    List<String> msg_list = new ArrayList<>();
                    List<String> phnerr_msgid = new ArrayList<>();
                    List<String> syserr_msgid = new ArrayList<>();
                    List<String> dateerr_msgid = new ArrayList<>();

                    List<RequestBean> sendList = new ArrayList<>();

                    for (RequestBean requestBean : _list) {

                        if(requestBean.getDateflag().equalsIgnoreCase("0")){
                            dateerr_msgid.add(requestBean.getMsgid());
                            continue;
                        }

                        if(StringUtils.isBlank(requestBean.getSyscd()) || StringUtils.isEmpty(requestBean.getSyscd())){
                            syserr_msgid.add(requestBean.getMsgid());
                            continue;
                        }

                        if(StringUtils.isBlank(requestBean.getSmssender())
                                || StringUtils.length(requestBean.getSmssender()) > senderMaxLen
                                || !requestBean.getSmssender().matches("^[0-9-]+$")){

                            phnerr_msgid.add(requestBean.getMsgid());
                            continue;
                        }

                        if(StringUtils.isBlank(requestBean.getPhn())
                                || StringUtils.length(requestBean.getPhn()) > receiverMaxLen
                                || !requestBean.getPhn().matches("^[0-9-]+$")){

                            phnerr_msgid.add(requestBean.getMsgid());
                            continue;
                        }

                        if(requestBean.getMsgsms().getBytes("EUC-KR").length > 90){
                            requestBean.setSmskind("L");
                        }

                        msg_list.add(requestBean.getMsgid());

                        sendList.add(requestBean);
                    }

                    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMM");
                    String ym = LocalDateTime.now().format(formatter2);

                    if(phnerr_msgid.size() > 0){
                        String strerrmsg = String.join(",", phnerr_msgid);

                        Msg_Log _ml = new Msg_Log(msgTable, logTable, mainTable, mainLogTable);
                        _ml.setMod_id(mod_id);
                        _ml.setMsgid(strerrmsg);

                        _ml.setLog_date_table(logTable+"_"+ym);

                        _ml.setResult_code("U010");
                        _ml.setResult_msg("번호 체크 오류처리");

                        requestService.phnErrUpdateDelete(_ml);
                    }

                    if(syserr_msgid.size() > 0){
                        String syserrmsg = String.join(",", syserr_msgid);

                        Msg_Log _ml = new Msg_Log(msgTable, logTable, mainTable, mainLogTable);
                        _ml.setMod_id(mod_id);
                        _ml.setMsgid(syserrmsg);

                        _ml.setLog_date_table(logTable+"_"+ym);

                        _ml.setResult_code("U005");
                        _ml.setResult_msg("등록되지 않은 시스템코드");

                        requestService.phnErrUpdateDelete(_ml);
                    }

                    if(dateerr_msgid.size() > 0){
                        String syserrmsg = String.join(",", dateerr_msgid);

                        Msg_Log _ml = new Msg_Log(msgTable, logTable, mainTable, mainLogTable);
                        _ml.setMod_id(mod_id);
                        _ml.setMsgid(syserrmsg);

                        _ml.setLog_date_table(logTable+"_"+ym);

                        _ml.setResult_code("U009");
                        _ml.setResult_msg("오늘보다 작은 발송일자 오류처리");

                        requestService.phnErrUpdateDelete(_ml);
                    }

                    if(sendList.size() > 0){
                        String strmsg = String.join(",", msg_list);

                        param.setMsgid_list(msg_list);
                        param.setStrmsgid(strmsg);

                        StringWriter sw = new StringWriter();
                        ObjectMapper om = new ObjectMapper();
                        om.writeValue(sw, sendList);

                        if(dbug.equalsIgnoreCase("Y")){
                            log.info("S/LMS data: " + sw.toString());
                        }

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
                                log.info("SMS/LMS 메세지 전송 완료(" + response.getStatusCode() + ") : "+ _list.size() + " 건");
                            } else {
                                log.error("SMS/LMS 메세지 전송 오류(Http ERR) : " + res.get("userid") + " / " + res.get("message"));
                                requestService.updateMSGSendInit(param);
                            }
                        } catch (Exception e) {
                            log.error("SMS/LMS 메세지 전송 오류(Response) : " + e.toString());
                            requestService.updateMSGSendInit(param);
                        }
                    }

                }
            }catch (Exception e){
                log.error("SMS/LMS 메세지 전송 오류(Send) : " + e.toString());
            }

            isProc = false;
        }
    }

    static public void setIsStart(boolean _flag) {
        log.info(role + " SMS/LMS Process is  change : " + _flag);
        isStart = _flag;
    }
}
