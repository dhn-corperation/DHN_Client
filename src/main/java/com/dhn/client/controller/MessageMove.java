package com.dhn.client.controller;

import com.dhn.client.bean.MoveData;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MessageMove implements ApplicationListener<ContextRefreshedEvent> {
    public static boolean isStart = false;
    private boolean isProc = false;
    private SQLParameter param = new SQLParameter();
    private String dhnServer;
    private String userid;
    private String preGroupNo = "";
    private String dual;
    private static String role;

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
        param.setMain_table(appContext.getEnvironment().getProperty("dhnclient.main_table"));
        param.setMod_id((appContext.getEnvironment().getProperty("dhnclient.mod_id")));

        userid = appContext.getEnvironment().getProperty("dhnclient.userid");
        dual = appContext.getEnvironment().getProperty("dhnclient.dual");
        role = appContext.getEnvironment().getProperty("dhnclient.role");

        if(dual != null && dual.equalsIgnoreCase("Y")){

        } else {
            isStart = true;
            log.info("Msg Move 초기화 완료");
        }
    }

    @Scheduled(fixedDelay = 100)
    private void MoveProcess() {
        if(isStart && !isProc) {
            isProc = true;

            try{
                int cnt = requestService.moveDataCount(param);

                if(cnt > 0){
                    List<MoveData> _list = requestService.moveDataSelect(param);

                    List<String> msgIdList = new ArrayList<>();
                    for(MoveData moveData : _list){
                        msgIdList.add(moveData.getMsgid());
                    }

                    param.setMsgid_list(msgIdList);

                    requestService.moveDataInsert(param);
                    requestService.updateMoveStatus(param);

                    log.info("{} 건 발송테이블 INSERT 완료",_list.size());


                }

            }catch (Exception e){
                log.error("Data Move 오류 : " + e.toString());
            }

            isProc = false;
        }
    }

    static public void setIsStart(boolean _flag) {
        log.info(role + " MESSAGE Move Process is change : " + _flag);
        isStart = _flag;
    }
}
