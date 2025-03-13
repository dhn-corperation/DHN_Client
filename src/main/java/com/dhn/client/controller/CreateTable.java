package com.dhn.client.controller;

import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Order(1)
public class CreateTable implements ApplicationListener<ContextRefreshedEvent> {

    private SQLParameter param = new SQLParameter();
    private String dual_flag = "N";

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
        param.setMain_table(appContext.getEnvironment().getProperty("dhnclient.main_table"));
        param.setLog_table(appContext.getEnvironment().getProperty("dhnclient.log_table"));
        dual_flag = appContext.getEnvironment().getProperty("dhnclient.dual");

        try{
            requestService.tableCheck(param);
            log.info("DHN 발송 테이블 체크 및 생성 완료");
        }catch (Exception e){
            log.error(param.getMsg_table() + " 테이블 생성 오류 : " + e.getMessage());
        }

        try{
            requestService.logTableCheck(param.getMsg_table(), param.getLog_table());
            log.info("DHN 로그 테이블 체크 및 생성 완료");
        }catch (Exception e){
            log.error(param.getLog_table() + " 테이블 생성 오류 : " + e.getMessage());
        }

        if(dual_flag.equalsIgnoreCase("Y")){
            param.setAlive_table(appContext.getEnvironment().getProperty("dhnclient.alive_table"));
            try{
                requestService.aliveTableCheck(param);
                log.info("DHN Alive 테이블 체크 및 생성 완료");
            }catch (Exception e){
                log.error(param.getAlive_table() + " 테이블 생성 오류 : " + e.getMessage());
            }
        }
    }
}
