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

        if(dual != null && dual.equalsIgnoreCase("Y")) {
            isStart = true;
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

                if( role != null && role.equalsIgnoreCase("MASTER")) {

                    int cnt = aliveService.AliveCount(param);

                    if(cnt == 0) {
                        aliveService.AliveInsert(param);
                    }

                    AliveStatusBean _as = aliveService.getAliveStatus(param);

                    if(_as.getStatus() != null && _as.getStatus().equalsIgnoreCase("MS")) {
                        param.setAlive_status(_as.getStatus().toUpperCase());
                        if(_as.getRole().equalsIgnoreCase("MASTER")) {
                            aliveService.AliveUpdate(param);

                            if(kakao_use.equalsIgnoreCase("Y") && !KAOSendRequest.isStart) {
                                KAOSendRequest.setIsStart(true);
                            }

                            if(sms_use.equalsIgnoreCase("Y") && !SMSSendRequest.isStart) {
                                SMSSendRequest.setIsStart(true);
                            }

                            if(lms_use.equalsIgnoreCase("Y") && !LMSSendRequest.isStart) {
                                LMSSendRequest.setIsStart(true);
                            }

                            if (slms_use.equalsIgnoreCase("Y") && !SLMSSendRequest.isStart) {
                                SLMSSendRequest.setIsStart(true);
                            }

                            if (tmp_use.equalsIgnoreCase("Y") && !TemplateRequest.isStart) {
                                tmp_use = tmp_use.toUpperCase();
                            }

                        } else {

                        }
                    } else if(_as.getStatus() != null && _as.getStatus().equalsIgnoreCase("SS")) {

                    }


                } else if( role != null && role.equalsIgnoreCase("SLAVE")) {

                }

            } catch (Exception e) {
                e.printStackTrace();
                log.error("Alive checked error : " + role);
            }

            isProc = false;
        }
    }


}
