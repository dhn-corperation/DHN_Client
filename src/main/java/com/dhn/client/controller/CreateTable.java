package com.dhn.client.controller;

import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Order(value = 1)
public class CreateTable implements ApplicationListener<ContextRefreshedEvent> {
    public static boolean isStart = false;
    private boolean isProc = false;
    private SQLParameter param = new SQLParameter();

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private ScheduledAnnotationBeanPostProcessor posts;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
        param.setLog_table(appContext.getEnvironment().getProperty("dhnclient.log_table"));
        param.setTran_msg_table(appContext.getEnvironment().getProperty("dhnclient.tran_msg_table"));
        param.setTran_log_table(appContext.getEnvironment().getProperty("dhnclient.tran_log_table"));
        param.setMms_msg_table(appContext.getEnvironment().getProperty("dhnclient.mms_msg_table"));
        param.setMms_log_table(appContext.getEnvironment().getProperty("dhnclient.mms_msg_log_table"));

        isStart = true;

    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void createTable() {
        if (isStart && !isProc) {
            log.info("로그 테이블 로그테이블 재확인 및 생성");
            isProc = true;
            try {
                requestService.logTableCheck(param);
            } catch (Exception e) {
                log.error("log 테이블 생성 오류 : " + e.getMessage());
            }
            isProc = false;
        }
    }
}
