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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class SMSSendRequest implements ApplicationListener<ContextRefreshedEvent> {

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

    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

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
        param.setSms_use(appContext.getEnvironment().getProperty("dhnclient.sms_use"));
        param.setMod_id((appContext.getEnvironment().getProperty("dhnclient.mod_id")));
        param.setMsg_type("M1");

        dhnServer = appContext.getEnvironment().getProperty("dhnclient.dhn_kakao_server");
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");
        dual = appContext.getEnvironment().getProperty("dhnclient.dual");
        role = appContext.getEnvironment().getProperty("dhnclient.role");
        dbug = appContext.getEnvironment().getProperty("dhnclient.dbug","N");

        msgTable = appContext.getEnvironment().getProperty("dhnclient.msg_table");
        logTable = appContext.getEnvironment().getProperty("dhnclient.log_table");
        mainTable = appContext.getEnvironment().getProperty("dhnclient.main_table");
        mainLogTable = appContext.getEnvironment().getProperty("dhnclient.main_log_table");
        mod_id = appContext.getEnvironment().getProperty("dhnclient.mod_id");

        if (param.getSms_use() != null && param.getSms_use().equalsIgnoreCase("Y")) {
            if(dual != null && dual.equalsIgnoreCase("Y")){

            }else{
                isStart = true;
                log.info("SMS 초기화 완료");
            }
        } else {
            posts.postProcessBeforeDestruction(this, null);
        }

        if(dbug.equalsIgnoreCase("Y")){
            log.info("SMS setting value : " + param.toString());
        }
    }

    @Scheduled(fixedDelay = 100)
    private void SendProcess() {
        if(isStart && !isProc) {
            isProc = true;

            ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) executorService;
            int activeThreads = poolExecutor.getActiveCount();

            if(activeThreads < 5){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
                LocalDateTime now = LocalDateTime.now();
                String group_no = "S"+now.format(formatter);

                if(!group_no.equals(preGroupNo)){
                    try{
                        int cnt = requestService.selectMSGRequestCount(param);

                        if(cnt > 0){

                            param.setGroup_no(group_no);
                            requestService.msgGroupUpdate(param);

                            executorService.submit(() -> APIProcess(group_no));

                        }
                    }catch (Exception e){
                        log.error("SMS 메세지 전송 오류(Send) : " + e.toString());
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
            sendParam.setMsg_table(msgTable);
            sendParam.setLog_table(logTable);
            sendParam.setMod_id(mod_id);

            List<RequestBean> _list = requestService.selectMSGRequests(sendParam);
            List<String> msg_list = new ArrayList<>();
            List<String> phnerr_msgid = new ArrayList<>();
            List<String> syserr_msgid = new ArrayList<>();
            List<String> dateerr_msgid = new ArrayList<>();

            List<RequestBean> sendList = new ArrayList<>();

            for (RequestBean bean : _list) {

                if(bean.getDateflag().equalsIgnoreCase("0")){
                    dateerr_msgid.add(bean.getMsgid());
                    continue;
                }

                if(StringUtils.isBlank(bean.getSyscd()) || StringUtils.isEmpty(bean.getSyscd())){
                    syserr_msgid.add(bean.getMsgid());
                    continue;
                }

                if(StringUtils.isBlank(bean.getSmssender())
                        || StringUtils.length(bean.getSmssender()) > senderMaxLen
                        || !bean.getSmssender().matches("^[0-9-]+$")){

                    phnerr_msgid.add(bean.getMsgid());
                    continue;
                }

                if(StringUtils.isBlank(bean.getPhn())
                        || StringUtils.length(bean.getPhn()) > receiverMaxLen
                        || !bean.getPhn().matches("^[0-9-]+$")){

                    phnerr_msgid.add(bean.getMsgid());
                    continue;
                }


                msg_list.add(bean.getMsgid());

                sendList.add(bean);
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
                log.info("SMS {} 건 번호체크 오류", phnerr_msgid.size());
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
                log.info("SMS {} 건 미등록 시스템코드", syserr_msgid.size());
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
                log.info("SMS {} 건 지난 발송일자", dateerr_msgid.size());
            }

            if(sendList.size() > 0){
                String strmsg = String.join(",", msg_list);

                sendParam.setMsgid_list(msg_list);
                sendParam.setStrmsgid(strmsg);

                StringWriter sw = new StringWriter();
                ObjectMapper om = new ObjectMapper();
                om.writeValue(sw, sendList);

                if(dbug.equalsIgnoreCase("Y")){
                    log.info("LMS data : " + sw.toString());
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
                        requestService.updateMSGSendComplete(sendParam);
                        log.info("SMS 메세지 전송 완료(" + response.getStatusCode() + ") : "+ _list.size() + " 건");
                    } else {
                        log.error("SMS 메세지 전송 오류(Http ERR) : " + res.get("userid") + " / " + res.get("message"));
                        requestService.updateMSGSendInit(sendParam);
                    }
                } catch (Exception e) {
                    log.error("SMS 메세지 전송 오류(Response) : " + e.toString());
                    requestService.updateMSGSendInit(sendParam);
                }
            }
        }catch (Exception e){
            log.error("MM 메세지 전송 오류(Send) : " + e.toString());
        }finally {
            if (executorService.isTerminated()) {
                executorService.shutdown();
                log.info("ExecutorService 종료 완료");
            }
        }
    }

    static public void setIsStart(boolean _flag) {
        log.info(role + " SMS Process is  change : " + _flag);
        isStart = _flag;
    }

}
