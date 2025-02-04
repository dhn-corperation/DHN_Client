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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MessageRtimeMove implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isProc = false;
    private SQLParameter param = new SQLParameter();
    private String dhnServer;
    private String userid;
    private String preGroupNo = "";
    private String dual;
    private String dbug = "N";
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
        dbug = appContext.getEnvironment().getProperty("dhnclient.dbug");

        userid = appContext.getEnvironment().getProperty("dhnclient.userid");
        dual = appContext.getEnvironment().getProperty("dhnclient.dual");
        role = appContext.getEnvironment().getProperty("dhnclient.role");

        if(dual != null && dual.equalsIgnoreCase("Y")){

        } else {
            isStart = true;
            log.info("Real Time Msg Move 초기화 완료");
        }
    }

    @Scheduled(fixedDelay = 100)
    private void MoveProcess() {
        if(isStart && !isProc) {
            isProc = true;

            if(dbug.equalsIgnoreCase("Y")){
                log.info("Real Time Move setting value : " + param.toString());
            }

            try{
                int cnt = requestService.moveRtimeDataCount(param);

                if(dbug.equalsIgnoreCase("Y")){
                    log.info("Real Time Move Cnt : " + cnt);
                }


                if(cnt > 0){
                    List<MoveData> _list = requestService.moveRtimeDataSelect(param);

                    List<String> msgIdList = new ArrayList<>();
                    for(MoveData moveData : _list){
                        msgIdList.add(moveData.getMsgid());
                    }

                    if(dbug.equalsIgnoreCase("Y")){
                        log.info("Move Real Time Data : " + param.getMsgid_list().toString());
                    }

                    String strmsg = String.join(",", msgIdList);

                    if(dbug.equalsIgnoreCase("Y")){
                        log.info("Move Real Time  msgid : " + strmsg);
                    }

                    param.setMsgid_list(msgIdList);
                    param.setStrmsgid(strmsg);

                    requestService.moveDataInsert(param);
                    requestService.updateMoveStatus(param);

                    log.info("Real Time {} 건 발송테이블 INSERT 완료",_list.size());


                }

            }catch (Exception e){
                log.error("Real Time Data Move 오류 : " + e.toString());
            }

            isProc = false;
        }
    }

    static public void setIsStart(boolean _flag) {
        log.info(role + " MESSAGE Real Time  Move Process is change : " + _flag);
        isStart = _flag;
    }
}
