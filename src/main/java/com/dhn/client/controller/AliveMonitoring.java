package com.dhn.client.controller;

import com.dhn.client.bean.AliveData;
import com.dhn.client.bean.AliveStatusBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.AliveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AliveMonitoring implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isProc = false;
    private String dual;
    private String role;
    private String role_type;
    private String kakao_use;
    private String sms_use;
    private String lms_use;
    private String slms_use;
    private String alive_table;

    @Autowired
    private AliveService aliveService;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    ScheduledAnnotationBeanPostProcessor posts;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        dual =  appContext.getEnvironment().getProperty("dhnclient.dual");
        role = appContext.getEnvironment().getProperty("dhnclient.role");
        kakao_use = appContext.getEnvironment().getProperty("dhnclient.kakao_use");
        sms_use = appContext.getEnvironment().getProperty("dhnclient.sms_use");
        lms_use = appContext.getEnvironment().getProperty("dhnclient.lms_use");
        slms_use = appContext.getEnvironment().getProperty("dhnclient.smslms_use");
        role_type = appContext.getEnvironment().getProperty("dhnclient.role_type");
        alive_table = appContext.getEnvironment().getProperty("dhnclient.alive_table");

        if(dual != null && dual.equalsIgnoreCase("Y")) {
            isStart = true;
            log.info("M/S 에이전트 이중화 준비 완료");
        } else {
            posts.postProcessBeforeDestruction(this, null);
        }
    }

    @Scheduled(fixedDelay = 10000)
    private void MonitoringProcess() {
        if(isStart && !isProc) {
            isProc = true;

            try{

                SQLParameter param = new SQLParameter();
                param.setRole(role);
                param.setRole_type(role_type);
                param.setAlive_table(alive_table);

                int cnt = aliveService.selectAliveCount(param);

                if(cnt == 0) {
                    aliveService.aliveInsertData(param);
                }

                AliveData aliveData = aliveService.selectAliveData(param);

                if(aliveData.getRole().equalsIgnoreCase(role)){
                    if(kakao_use != null && kakao_use.equalsIgnoreCase("Y")) {
                        if(!KAOSendRequest.isStart) {
                            KAOSendRequest.setIsStart(true);
                        }

                        if(!KAORealTimeSendRequest.isStart) {
                            KAORealTimeSendRequest.setIsStart(true);
                        }
                    }

                    if(sms_use != null && sms_use.equalsIgnoreCase("Y")) {
                        if(!SMSSendRequest.isStart) {
                            SMSSendRequest.setIsStart(true);
                        }

                        if(!SMSRealTimeSendRequest.isStart) {
                            SMSRealTimeSendRequest.setIsStart(true);
                        }
                    }

                    if(lms_use != null && lms_use.equalsIgnoreCase("Y")) {
                        if(!LMSSendRequest.isStart) {
                            LMSSendRequest.setIsStart(true);
                        }

                        if(!LMSRealTimeSendRequest.isStart) {
                            LMSRealTimeSendRequest.setIsStart(true);
                        }
                    }

                    if (slms_use != null && slms_use.equalsIgnoreCase("Y")) {
                        if(!SLMSSendRequest.isStart) {
                            SLMSSendRequest.setIsStart(true);
                        }

                        if(!SLMSRealTimeSendRequest.isStart) {
                            SLMSRealTimeSendRequest.setIsStart(true);
                        }
                    }

                    if (slms_use != null && slms_use.equalsIgnoreCase("Y")) {
                        if(!MMSendRequest.isStart) {
                            MMSendRequest.setIsStart(true);
                        }

                        if(!MMRealTimeSendRequest.isStart) {
                            MMRealTimeSendRequest.setIsStart(true);
                        }
                    }

                    if(!LogTableCheck.isStart){
                        LogTableCheck.setIsStart(true);
                    }

                    if(!ResultReq.isStart){
                        ResultReq.setIsStart(true);
                    }

                    if(!ResultOTPReq.isStart){
                        ResultOTPReq.setIsStart(true);
                    }

                    aliveService.aliveUpdateDate(param);

                }else{
                    if(aliveData.getFlag().equalsIgnoreCase("Y")){
                        if(kakao_use != null && kakao_use.equalsIgnoreCase("Y")) {
                            if(!KAOSendRequest.isStart) {
                                KAOSendRequest.setIsStart(true);
                            }

                            if(!KAORealTimeSendRequest.isStart) {
                                KAORealTimeSendRequest.setIsStart(true);
                            }
                        }

                        if(sms_use != null && sms_use.equalsIgnoreCase("Y")) {
                            if(!SMSSendRequest.isStart) {
                                SMSSendRequest.setIsStart(true);
                            }

                            if(!SMSRealTimeSendRequest.isStart) {
                                SMSRealTimeSendRequest.setIsStart(true);
                            }
                        }

                        if(lms_use != null && lms_use.equalsIgnoreCase("Y")) {
                            if(!LMSSendRequest.isStart) {
                                LMSSendRequest.setIsStart(true);
                            }

                            if(!LMSRealTimeSendRequest.isStart) {
                                LMSRealTimeSendRequest.setIsStart(true);
                            }
                        }

                        if (slms_use != null && slms_use.equalsIgnoreCase("Y")) {
                            if(!SLMSSendRequest.isStart) {
                                SLMSSendRequest.setIsStart(true);
                            }

                            if(!SLMSRealTimeSendRequest.isStart) {
                                SLMSRealTimeSendRequest.setIsStart(true);
                            }
                        }

                        if (slms_use != null && slms_use.equalsIgnoreCase("Y")) {
                            if(!MMSendRequest.isStart) {
                                MMSendRequest.setIsStart(true);
                            }

                            if(!MMRealTimeSendRequest.isStart) {
                                MMRealTimeSendRequest.setIsStart(true);
                            }
                        }

                        if(!LogTableCheck.isStart){
                            LogTableCheck.setIsStart(true);
                        }

                        if(!ResultReq.isStart){
                            ResultReq.setIsStart(true);
                        }

                        if(!ResultOTPReq.isStart){
                            ResultOTPReq.setIsStart(true);
                        }

                        aliveService.aliveUpdateAgent(param);
                    } else {
                        if(KAOSendRequest.isStart) {
                            KAOSendRequest.setIsStart(false);
                        }

                        if(KAORealTimeSendRequest.isStart){
                            KAORealTimeSendRequest.setIsStart(false);
                        }

                        if(SMSSendRequest.isStart) {
                            SMSSendRequest.setIsStart(false);
                        }

                        if(SMSRealTimeSendRequest.isStart){
                            SMSRealTimeSendRequest.setIsStart(false);
                        }

                        if(LMSSendRequest.isStart) {
                            LMSSendRequest.setIsStart(false);
                        }

                        if(LMSRealTimeSendRequest.isStart){
                            LMSRealTimeSendRequest.setIsStart(false);
                        }

                        if (SLMSSendRequest.isStart) {
                            SLMSSendRequest.setIsStart(false);
                        }

                        if(SLMSSendRequest.isStart){
                            SLMSSendRequest.setIsStart(false);
                        }

                        if (MMSendRequest.isStart){
                            MMSendRequest.setIsStart(false);
                        }

                        if(MMRealTimeSendRequest.isStart){
                            MMRealTimeSendRequest.setIsStart(false);
                        }

                        if(LogTableCheck.isStart) {
                            LogTableCheck.setIsStart(false);
                        }

                        if(ResultReq.isStart) {
                            ResultReq.setIsStart(false);
                        }

                        if(ResultOTPReq.isStart){
                            ResultOTPReq.setIsStart(false);
                        }
                    }
                }

            }catch (Exception e) {
                log.error("Alive checked error : " + role + " / " + e.getMessage());
            }

            isProc = false;
        }
    }

}
