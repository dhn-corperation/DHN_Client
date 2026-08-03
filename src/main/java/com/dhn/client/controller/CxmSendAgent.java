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
public class CxmSendAgent extends AbstractSendAgent {

    @Autowired
    @Qualifier("cxmService")
    private RequestService requestService;

    // ⭐️ CXM 마스터 스위치 및 yml 세팅
    @Value("${dhnclient.cxm_use:N}") private String cxmUse;
    @Value("${dhnclient.cxm.userid:}") private String userid;
    @Value("${dhnclient.server:}") private String dhnServer;
    @Value("${dhnclient.cxm.db-target:oracle}") private String dbTarget;
    @Value("${dhnclient.cxm.msg_table:EMFO_DATA}") private String msgTable;
    @Value("${dhnclient.cxm.log_table:EMFO_LOG}") private String logTable;

    // ⭐️ 수신거부 번호 (yml에서 주입, 없으면 기본값)
    @Value("${dhnclient.block_sender:080-1234-5678}") private String blockSender;

    // ⏰ CXM 채널: 알림톡(AT), 친구톡(FT), LMS 순회!
    @Scheduled(fixedDelay = 1000)
    public void SendProcess() {
        if (!"Y".equalsIgnoreCase(cxmUse)) return; // 스위치 On일 때만 동작

        String[] msgTypes = {"AT", "FT", "LMS"};
        for (String msgType : msgTypes) {
            super.executeProcess(this.dhnServer, this.userid, msgType);
        }
    }

    @Override protected String getChannelName() { return "CXM"; }
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

                // =========================================================
                // 🚀 [특이사항] 광고문자(LMS + ETC5='Y') 본문 강제 조립 로직
                // =========================================================
                if ("LMS".equalsIgnoreCase(msgType) && "Y".equalsIgnoreCase(bean.getEtc5())) {
                    String originalMsg = bean.getMsg();
                    // 프리픽스와 서픽스 조립
                    String adMsg = "(광고) 강원랜드\n" + originalMsg + "\n무료수신거부: " + blockSender;
                    bean.setMsg(adMsg);

                    // 메시지 타입 셋팅
                    bean.setMessagetype("LM");
                } else if ("AT".equalsIgnoreCase(msgType)) {
                    bean.setMessagetype("AT");
                } else if ("FT".equalsIgnoreCase(msgType)) {
                    bean.setMessagetype("FT");
                }
                // =========================================================

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
            log.error("[CXM - {}] 데이터 조회/정제 오류: {}", msgType, e.getMessage());
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
            log.error("[CXM] 상태값 업데이트 오류: {}", e.getMessage());
        }
    }
}