package com.dhn.client.controller;

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
    private String tmp_use;
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
        tmp_use = appContext.getEnvironment().getProperty("dhnclient.tmp_use");
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

                if( role != null && role.equalsIgnoreCase("MASTER")) {

                    int cnt = aliveService.AliveCount(param);

                    if(cnt == 0) {
                        aliveService.AliveInsert(param);
                    }

                    AliveStatusBean _as = aliveService.getAliveStatus(param);

                    if(_as.getStatus() != null && _as.getStatus().equalsIgnoreCase("MS")) {
                        param.setAlive_status(_as.getStatus().toUpperCase());
                        if(_as.getRole().equalsIgnoreCase("MASTER")) {

                            if(kakao_use != null && kakao_use.equalsIgnoreCase("Y") && !KAOSendRequest.isStart) {
                                KAOSendRequest.setIsStart(true);
                            }

                            if(sms_use != null && sms_use.equalsIgnoreCase("Y") && !SMSSendRequest.isStart) {
                                SMSSendRequest.setIsStart(true);
                            }

                            if(lms_use != null && lms_use.equalsIgnoreCase("Y") && !LMSSendRequest.isStart) {
                                LMSSendRequest.setIsStart(true);
                            }

                            if (slms_use != null && slms_use.equalsIgnoreCase("Y") && !SLMSSendRequest.isStart) {
                                SLMSSendRequest.setIsStart(true);
                            }

                            if (tmp_use != null && tmp_use.equalsIgnoreCase("Y") && !TemplateRequest.isStart) {
                                TemplateRequest.setIsStart(true);
                            }

                            if(!ResultReq.isStart){
                                ResultReq.setIsStart(true);
                            }

                            if(!LogTableCheck.isStart){
                                LogTableCheck.setIsStart(true);
                            }

                            if(!MessageMove.isStart){
                                MessageMove.setIsStart(true);
                            }

                            if(!ResultReq.isStart){
                                ResultReq.setIsStart(true);
                            }

                            aliveService.AliveUpdate(param);

                        } else {

                            if(kakao_use != null && kakao_use.equalsIgnoreCase("Y")) {
                                KAOSendRequest.setIsStart(true);
                            }

                            if(sms_use != null && sms_use.equalsIgnoreCase("Y")) {
                                SMSSendRequest.setIsStart(true);
                            }

                            if(lms_use != null && lms_use.equalsIgnoreCase("Y")) {
                                LMSSendRequest.setIsStart(true);
                            }

                            if (slms_use != null && slms_use.equalsIgnoreCase("Y")) {
                                SLMSSendRequest.setIsStart(true);
                            }

                            if (tmp_use != null && tmp_use.equalsIgnoreCase("Y")) {
                                TemplateRequest.setIsStart(true);
                            }

                            LogTableCheck.setIsStart(true);
                            MessageMove.setIsStart(true);
                            ResultReq.setIsStart(true);

                            aliveService.AliveUpdate(param);

                        }
                    } else if(_as.getStatus() != null && _as.getStatus().equalsIgnoreCase("SS")) {
                        param.setAlive_status("MW");
                        aliveService.AliveUpdate(param);
                    }


                } else if( role != null && role.equalsIgnoreCase("SLAVE")) {

                    int cnt = aliveService.AliveCount(param);

                    if(cnt > 0){
                        int isAlive = aliveService.AliveLastCount(param);

                        if(isAlive == 0) {
                            AliveStatusBean _as = aliveService.getAliveStatus(param);
                            if(_as.getRole().equalsIgnoreCase("MASTER") && _as.getStatus().equalsIgnoreCase("MS")){

                                if(kakao_use != null && kakao_use.equalsIgnoreCase("Y")) {
                                    KAOSendRequest.setIsStart(true);
                                }

                                if(sms_use != null && sms_use.equalsIgnoreCase("Y")) {
                                    SMSSendRequest.setIsStart(true);
                                }

                                if(lms_use != null && lms_use.equalsIgnoreCase("Y")) {
                                    LMSSendRequest.setIsStart(true);
                                }

                                if (slms_use != null && slms_use.equalsIgnoreCase("Y")) {
                                    SLMSSendRequest.setIsStart(true);
                                }

                                if (tmp_use != null && tmp_use.equalsIgnoreCase("Y")) {
                                    TemplateRequest.setIsStart(true);
                                }

                                LogTableCheck.setIsStart(true);
                                MessageMove.setIsStart(true);
                                ResultReq.setIsStart(true);

                                param.setAlive_status("SS");

                                aliveService.AliveUpdate(param);

                            } else if (_as.getStatus().equalsIgnoreCase("MW")){
                                if(kakao_use != null && kakao_use.equalsIgnoreCase("Y")) {
                                    KAOSendRequest.setIsStart(false);
                                }

                                if(sms_use != null && sms_use.equalsIgnoreCase("Y")) {
                                    SMSSendRequest.setIsStart(false);
                                }

                                if(lms_use != null && lms_use.equalsIgnoreCase("Y")) {
                                    LMSSendRequest.setIsStart(false);
                                }

                                if (slms_use != null && slms_use.equalsIgnoreCase("Y")) {
                                    SLMSSendRequest.setIsStart(false);
                                }

                                if (tmp_use != null && tmp_use.equalsIgnoreCase("Y")) {
                                    TemplateRequest.setIsStart(false);
                                }

                                LogTableCheck.setIsStart(false);
                                MessageMove.setIsStart(false);
                                ResultReq.setIsStart(false);

                                if(!KAOSendRequest.isStart && !SMSSendRequest.isStart && !LMSSendRequest.isStart && !SLMSSendRequest.isStart && !TemplateRequest.isStart && !LogTableCheck.isStart && !MessageMove.isStart && !ResultReq.isStart){
                                    param.setAlive_status("MS");
                                    aliveService.AliveUpdate(param);
                                }
                            }
                        }
                    }


                }

            } catch (Exception e) {
                e.printStackTrace();
                log.error("Alive checked error : " + role);
            }

            isProc = false;
        }
    }


}
