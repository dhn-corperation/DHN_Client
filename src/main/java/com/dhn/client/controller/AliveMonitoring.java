package com.dhn.client.controller;

import com.dhn.client.bean.AliveData;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.AliveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AliveMonitoring implements ApplicationListener<ContextRefreshedEvent> {
    public static boolean isStart = false;
    private boolean isProc = false;
    private String dual = "N";
    private String role;
    private String role_type;
    private String alive_table;
    private String dbtype;

    @Autowired
    private AliveService aliveService;

    @Autowired
    private ApplicationContext appContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        dual =  appContext.getEnvironment().getProperty("dhnclient.dual","N");
        role = appContext.getEnvironment().getProperty("dhnclient.role");
        role_type = appContext.getEnvironment().getProperty("dhnclient.role_type");
        alive_table = appContext.getEnvironment().getProperty("dhnclient.alive_table");
        dbtype = appContext.getEnvironment().getProperty("dhnclient.database");

        if(dual.equalsIgnoreCase("Y")) {
            isStart = true;
            log.info("M/S 에이전트 이중화 준비 완료");
        } else {

        }
    }

    @Scheduled(fixedDelay = 10000)
    private void MonitoringProcess(){
        if(isStart && !isProc) {
            isProc = true;

            try{

                SQLParameter param = new SQLParameter();
                param.setRole(role);
                param.setRole_type(role_type);
                param.setAlive_table(alive_table);
                param.setDBType(dbtype);

                int cnt = aliveService.selectAliveCount(param);

                if(cnt == 0) {
                    aliveService.aliveInsertData(param);
                }

                AliveData aliveData = aliveService.selectAliveData(param);

                if(aliveData.getRole().equalsIgnoreCase(role)){
                    if(!MMSSendRequest.isStart) {
                        MMSSendRequest.setIsStart(true);
                    }

                    if(!SMSSendRequest.isStart) {
                        SMSSendRequest.setIsStart(true);
                    }

                    if(!ResultReq.isStart){
                        ResultReq.setIsStart(true);
                    }

                    aliveService.aliveUpdateDate(param);

                }else{
                    if(aliveData.getFlag().equalsIgnoreCase("Y")){
                        if(!MMSSendRequest.isStart) {
                            MMSSendRequest.setIsStart(true);
                        }

                        if(!SMSSendRequest.isStart) {
                            SMSSendRequest.setIsStart(true);
                        }

                        if(!ResultReq.isStart){
                            ResultReq.setIsStart(true);
                        }

                        aliveService.aliveUpdateAgent(param);
                    } else {
                        if(MMSSendRequest.isStart) {
                            MMSSendRequest.setIsStart(false);
                        }

                        if(SMSSendRequest.isStart) {
                            SMSSendRequest.setIsStart(false);
                        }

                        if(ResultReq.isStart) {
                            ResultReq.setIsStart(false);
                        }
                    }
                }

            }catch (Exception e) {
                log.error("Alive checked error : " + role + " / " + e.getMessage());
            }

            isProc = false;
        }
    }
}
