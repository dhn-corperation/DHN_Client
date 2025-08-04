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
    private String dual = "";
    private String kakao_use;
    private static String role = "";

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private RequestService requestService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        msg_table = appContext.getEnvironment().getProperty("dhnclient.msg_table");
        log_table = appContext.getEnvironment().getProperty("dhnclient.log_table");
        dual = appContext.getEnvironment().getProperty("dhnclient.dual");
        role = appContext.getEnvironment().getProperty("dhnclient.role");
        kakao_use = appContext.getEnvironment().getProperty("dhnclient.kakao_use");

        if(dual != null && dual.equalsIgnoreCase("Y")){

        } else {
            isStart = true;
            log.info("LOG테이블 자동생성 초기화 완료");
        }

    }

    @Scheduled(cron = "0 0 * * * ?")
    public void createTable() {
        if (kakao_use.equalsIgnoreCase("Y")) {
            log.info("Log Table Create kakao_use 활성화 → 30초 대기 시작");
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("Log Table Create kakao_use 활성화 → 30초 대기 완료 후 실행");
        }

        log.info("로그테이블 재확인 및 생성");

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

    static public void setIsStart(boolean _flag) {
        log.info(role + " LogTable create Process is change : " + _flag);
        isStart = _flag;
    }
}
