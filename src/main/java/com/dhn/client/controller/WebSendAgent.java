package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WebSendAgent extends AbstractSendAgent {

    @Autowired
    @Qualifier("webService")
    private RequestService requestService;

    @Value("${dhnclient.web_use:N}") private String webUse;
    @Value("${dhnclient.web.userid:}") private String userid;
    @Value("${dhnclient.server:}") private String dhnServer;
    @Value("${dhnclient.web.db-target:mssql}") private String dbTarget;
    @Value("${dhnclient.web.msg_table:SUREDATA}") private String msgTable;
    @Value("${dhnclient.web.log_table:SUREDATA_LOG}") private String logTable;

    // ⏰ 알림톡(T) 타입 타겟으로 스케줄러 구동
    @Scheduled(fixedDelay = 1000)
    public void SendProcess() {
        if (!"Y".equalsIgnoreCase(webUse)) return;

        // DB의 KIND 컬럼이 'T' (카카오비즈메시지/알림톡)인 것을 타겟팅
        String[] msgTypes = {"T"};
        for (String msgType : msgTypes) {
            super.executeProcess(this.dhnServer, this.userid, msgType);
        }
    }

    @Override protected String getChannelName() { return "WEB"; }
    @Override protected String getDbTarget() { return this.dbTarget; }

    @Override
    protected List<RequestBean> fetchWaitingData(String msgType) {
        List<RequestBean> finalSendList = new ArrayList<>();
        List<String> invalidList = new ArrayList<>();

        try {
            SQLParameter param = new SQLParameter();
            param.setMsg_table(msgTable);
            param.setMsg_type(msgType);
            param.setDatabase(dbTarget);

            List<RequestBean> rawList = requestService.selectRequests(param);
            if (rawList == null || rawList.isEmpty()) return finalSendList;

            ObjectMapper mapper = new ObjectMapper();

            for (RequestBean bean : rawList) {
                if (bean.getPhn() == null || bean.getPhn().length() < 10) {
                    invalidList.add(bean.getMsgid());
                    continue;
                }

                bean.setMessagetype("AT"); // 알림톡 타입 지정

                try {
                    byte[] msgBytes = bean.getMsg() != null ? bean.getMsg().getBytes("EUC-KR") : new byte[0];
                    if (msgBytes.length > 90) {
                        bean.setSmskind("L");
                    } else {
                        bean.setSmskind("S");
                    }
                } catch (Exception e) {
                    bean.setSmskind("L"); // 예외 시 기본 단문 처리
                }

                boolean isGoodData = bean.processJsonPayload(mapper, invalidList);
                if (isGoodData) {
                    finalSendList.add(bean);
                }
            }

            if (!invalidList.isEmpty()) {
                Msg_Log ml = new Msg_Log();
                ml.setMsg_table(msgTable);
                ml.setLog_table(logTable);
                ml.setStatus("4");
                ml.setCode("7999");
                ml.setDatabase(dbTarget);
                requestService.updateInvalidData(invalidList, ml);
            }
        } catch (Exception e) {
            log.error("[WEB-알림톡] 데이터 조회/정제 오류: {}", e.getMessage());
        }
        return finalSendList;
    }

    @Override
    protected void updateStatusToSent(List<String> msgIds) {
        try {
            SQLParameter param = new SQLParameter();
            param.setMsg_table(msgTable);
            param.setMsgIds(msgIds);
            param.setDatabase(dbTarget);
            requestService.updateSendComplete(param);
        } catch (Exception e) {
            log.error("[WEB-알림톡] 상태값 업데이트 오류: {}", e.getMessage());
        }
    }
}