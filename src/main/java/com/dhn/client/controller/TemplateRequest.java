package com.dhn.client.controller;

import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.TemplateReqSevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TemplateRequest implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isCProc = false;
    private boolean isUProc = false;
    private boolean isDProc = false;
    private boolean isRProc = false;
    private SQLParameter param = new SQLParameter();
    private String dhnServer;
    private String userid;

    @Autowired
    private TemplateReqSevice templateReqSevice;

    @Autowired
    private ApplicationContext appContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setTmp_table(appContext.getEnvironment().getProperty("dhnclient.tmp_table"));
        param.setTmp_use(appContext.getEnvironment().getProperty("dhnclient.tmp_use"));

        dhnServer = appContext.getEnvironment().getProperty("dhnclient.dhn_kakao_server");
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");

        if (param.getTmp_use() != null && param.getTmp_use().equalsIgnoreCase("Y")) {
            log.info("Tmp 초기화 완료");
            isStart = true;
        }
    }

    @Scheduled(fixedDelay = 60000)
    private void CreateTemplate() {
        if(isStart && !isCProc) {
            isCProc = true;
            log.info("CreateTemplate 실행");

            try{

            }catch (Exception e){

            }
            isCProc = false;
        }
    }

    @Scheduled(fixedDelay = 60000)
    private void UpdateTemplate() {
        if(isStart && !isUProc) {
            isUProc = true;
            log.info("UpdateTemplate 실행");

            try{

            }catch (Exception e){

            }
            isUProc = false;
        }
    }

    @Scheduled(fixedDelay = 60000)
    private void DeleteTemplate() {
        if(isStart && !isDProc) {
            isDProc = true;
            log.info("DeleteTemplate 실행");

            try{

            }catch (Exception e){

            }
            isDProc = false;
        }
    }

    @Scheduled(cron = "0 0 1 * * *")
    private void refreshTemplate() {
        if(isStart && !isRProc) {
            isRProc = true;
            log.info("refreshTemplate 실행");

            try{

            }catch (Exception e){

            }
            isRProc = false;
        }
    }
}
