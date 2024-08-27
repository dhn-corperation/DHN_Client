package com.dhn.client.controller;

import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class Log_Move implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isProc = false;
    private SQLParameter param = new SQLParameter();
    private String preGroupNo = "";

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
        param.setLog_table(appContext.getEnvironment().getProperty("dhnclient.log_table"));

        isStart = true;
    }

    @Scheduled(fixedDelay = 100)
    private void LogRemove() {
        if(isStart && !isProc) {
            isProc = true;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
            LocalDateTime now = LocalDateTime.now();
            String group_no = "9" + now.format(formatter);

            if(!group_no.equals(preGroupNo)){
                try {
                    int cnt = requestService.log_move_count(param);
                    if(cnt > 0){

                        param.setGroup_no(group_no);

                        requestService.update_log_move_groupNo(param);

                        requestService.log_move(param);

                        log.info("Log 테이블 이동 그룹 : {}",param.getGroup_no());
                    }
                }catch (Exception e){
                    log.error("Log 테이블로 이동중 오류 발생 : " + e.toString());
                }

                preGroupNo = group_no;
            }
            isProc = false;
        }
    }
}
