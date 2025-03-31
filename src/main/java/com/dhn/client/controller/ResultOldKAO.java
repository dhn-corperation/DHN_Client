package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.OldResultBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ResultOldKAO implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isProc = false;
    private String kakaot = "";
    private String kakaotl = "";
    private String tableseq = "";
    private SQLParameter param = new SQLParameter();
    private String oldflag = "N";

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApplicationContext appContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        kakaot = appContext.getEnvironment().getProperty("dhnclient.req_table");
        kakaotl = appContext.getEnvironment().getProperty("dhnclient.log_table");
        tableseq = appContext.getEnvironment().getProperty("dhnclient.table_seq");

        param.setMsg_table( appContext.getEnvironment().getProperty("dhnclient.req_table") );
        param.setZbsysmcd_table(appContext.getEnvironment().getProperty("dhnclient.zbsysmcd_table") );
        param.setKakao( appContext.getEnvironment().getProperty("dhnclient.kakao") );

        param.setMsg_type("6");
        param.setTime("2");

        oldflag = appContext.getEnvironment().getProperty("dhnclient.oldflag");

        if(oldflag.equalsIgnoreCase("Y")){
            isStart = true;
            log.info("KAO OLD DATA 프로세스 초기화 완료");
        }
    }

    @Scheduled(fixedDelay = 60000)
    private void SendProcess() {
        if(isStart && !isProc) {
            isProc = true;

            try{
                int cnt = requestService.selectOldDataCount(param);

                if(cnt > 0){
                    List<OldResultBean> oldList = requestService.selectOldData(param);

                    int count = 0;

                    for (OldResultBean oldResultBean : oldList) {

                        Msg_Log ml = new Msg_Log(kakaot, kakaotl);
                        ml.setMseq (oldResultBean.getMsgid());
                        ml.setTelecom("KAK");
                        ml.setStat(oldResultBean.getStat());
                        ml.setPseq(oldResultBean.getPseq());

                        try{
                            requestService.oldDataResult(ml);
                            log.info("KAO 과거 데이터 결과처리 Mseq : " + ml.getMseq());
                            count++;
                        }catch (Exception e){
                            log.error("KAO Old Data Result Error (Update - Insert - Delete) : " + e.toString());
                        }
                    }

                    log.info("KAO 과거데이터 결과처리 완료 {}건",count);
                }

            } catch (Exception e){
                log.error("KAO Old Data Result Error : " + e.toString());
            }

            isProc = false;
        }
    }
}
