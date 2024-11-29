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
public class Test implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isProc = false;
    //private SQLParameter param = new SQLParameter();
    private String dhnServer;
    private String userid;
    //private Map<String, String> _rsltCode = new HashMap<String, String>();
    private static int procCnt = 0;
    private String msgTable = "";
    private String logTable = "";
    private String dbtype = "";

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        msgTable = appContext.getEnvironment().getProperty("dhnclient.msg_table");
        logTable = appContext.getEnvironment().getProperty("dhnclient.log_table");
        dbtype = appContext.getEnvironment().getProperty("dhnclient.database");

        dhnServer = "http://" + appContext.getEnvironment().getProperty("dhnclient.server") + "/";
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");

        isStart = true;
    }

    @Scheduled(fixedDelay = 100)
    public void start() {
        try{
            String text = null;
            log.info("{}",text.length());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
