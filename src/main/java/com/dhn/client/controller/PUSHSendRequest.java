package com.dhn.client.controller;

import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.KAOService;
import com.dhn.client.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PUSHSendRequest implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isProc = false;
    private SQLParameter param = new SQLParameter();
    private String dhnServer;
    private String userid;
    private String preGroupNo = "";
    private String crypto = "";

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private KAOService kaoService;

    @Autowired
    ScheduledAnnotationBeanPostProcessor posts;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
        param.setKakao_use(appContext.getEnvironment().getProperty("dhnclient.kakao_use"));
        param.setProfile_key(appContext.getEnvironment().getProperty("dhnclient.kakao_profile_key"));
        param.setMsg_type("K");

        dhnServer = "http://" + appContext.getEnvironment().getProperty("dhnclient.dhn_kakao_server") + "/";
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");
        crypto = appContext.getEnvironment().getProperty("dhnclient.crypto");

        if (param.getKakao_use() != null && param.getKakao_use().toUpperCase().equals("Y")) {
            log.info("KAO 초기화 완료");
            isStart = true;
        } else {
            posts.postProcessBeforeDestruction(this, null);
        }
    }

}
