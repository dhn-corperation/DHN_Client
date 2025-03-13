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
    private String dbug = "N";
    private static String role;
    private String kakao_use;
    private String sms_use;
    private String lms_use;
    private String smslms_use;

    private List<String> msg_type = new ArrayList<>();

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        param.setMsg_table(appContext.getEnvironment().getProperty("dhnclient.msg_table"));
        param.setMain_table(appContext.getEnvironment().getProperty("dhnclient.main_table"));
        param.setMod_id((appContext.getEnvironment().getProperty("dhnclient.mod_id")));
        dbug = appContext.getEnvironment().getProperty("dhnclient.dbug","N");

        userid = appContext.getEnvironment().getProperty("dhnclient.userid");
        dual = appContext.getEnvironment().getProperty("dhnclient.dual");
        role = appContext.getEnvironment().getProperty("dhnclient.role");

        kakao_use = appContext.getEnvironment().getProperty("dhnclient.kakao_use");
        sms_use = appContext.getEnvironment().getProperty("dhnclient.sms_use");
        lms_use = appContext.getEnvironment().getProperty("dhnclient.lms_use");
        smslms_use = appContext.getEnvironment().getProperty("dhnclient.smslms_use");

        if(kakao_use != null && kakao_use.equals("Y")){
            msg_type.add("K1");
        }

        if(sms_use != null && sms_use.equals("Y")){
            msg_type.add("M1");
        }

        if(lms_use != null && lms_use.equals("Y")){
            msg_type.add("M2");
        }

        if(smslms_use != null && smslms_use.equals("Y")){
            msg_type.add("99");
            msg_type.add("MM");
        }

        param.setFlag_msg_type("'"+String.join("','", msg_type)+"'");

        if(dbug != null && dbug.equals("Y")){
            log.info(param.toString());
        }

        if(dual != null && dual.equalsIgnoreCase("Y")){

        } else {
            if((kakao_use != null && kakao_use.equals("Y")) ||
                    (sms_use != null && sms_use.equals("Y")) ||
                    (lms_use != null && lms_use.equals("Y")) ||
                    (smslms_use != null && smslms_use.equals("Y"))){
                isStart = true;
                log.info("Msg Move 초기화 완료");
            }
        }

        if(dbug.equalsIgnoreCase("Y")){
            log.info("Move setting value : " + param.toString());
        }
    }

    @Scheduled(fixedDelay = 100)
    private void MoveProcess() {
        if(isStart && !isProc) {
            isProc = true;

            try{
                int cnt = requestService.moveDataCount(param);

                if(dbug.equalsIgnoreCase("Y")){
                    log.info("Move Cnt : " + cnt);
                }

                if(cnt > 0){
                    List<MoveData> _list = requestService.moveDataSelect(param);

                    List<String> msgIdList = new ArrayList<>();

                    for(MoveData moveData : _list){
                        msgIdList.add(moveData.getMsgid());
                    }
                    if(dbug.equalsIgnoreCase("Y")){
                        log.info("Move Data : " + msgIdList.toString());
                    }

                    String strmsg = String.join(",", msgIdList);
                    if(dbug.equalsIgnoreCase("Y")){
                        log.info("Move msgid : " + strmsg);
                    }

                    param.setMsgid_list(msgIdList);
                    param.setStrmsgid(strmsg);

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
