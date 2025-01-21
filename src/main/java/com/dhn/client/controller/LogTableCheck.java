package com.dhn.client.controller;

import com.dhn.client.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogTableCheck implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isProc = false;
    private String msg_table = "";
    private String log_table = "";

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private RequestService requestService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        msg_table = appContext.getEnvironment().getProperty("msg_table");
        log_table = appContext.getEnvironment().getProperty("log_table");

        log.info("LOG테이블 자동생성 초기화 완료");

        isStart = true;
    }

    @Scheduled(cron = "0 0 1 L * ?")
    public void createTable() {
        log.info("로그 테이블 로그테이블 재확인 및 생성");
        if(isStart && !isProc){
            isProc = true;
            try{

                requestService.logTableCheck(msg_table, log_table);

            }catch (Exception e){
                log.error("log 테이블 생성 오류 : "+e.getMessage());
            }
            isProc = false;
        }
    }
}
