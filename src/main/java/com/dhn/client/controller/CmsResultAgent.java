package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.config.DbContextHolder;
import com.dhn.client.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CmsResultAgent extends AbstractResultAgent {

    @Autowired
    @Qualifier("cmsService")
    protected RequestService requestService;

    @Value("${dhnclient.cms_use:N}") private String cmsUse;
    @Value("${dhnclient.cms.userid:}") private String userid;
    @Value("${dhnclient.server:}") private String dhnServer;
    @Value("${dhnclient.cms.db-target:oracle}") private String dbTarget;
    @Value("${dhnclient.cms.msg_table:TBL_SUBMIT_QUEUE}") private String msgTable;
    @Value("${dhnclient.cms.log_table:TBL_MSG_HIST}") private String logTable;

    @PostConstruct
    public void init() {
        if ("Y".equalsIgnoreCase(cmsUse)) {
            super.initAgent();
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void runResultProcess() {
        if (!"Y".equalsIgnoreCase(cmsUse)) {
            return;
        }

        DbContextHolder.setDbTarget(dbTarget);
        try {
            super.executeResultProcess();
        } finally {
            DbContextHolder.clear();
        }
    }

    @Override
    protected void resultProc(JSONArray json) {
        log.info("[CMS] 결과 처리 시작 [ {} ] 건", json.length());

        for (int i = 0; i < json.length(); i++) {
            JSONObject ent = json.getJSONObject(i);

            Msg_Log _ml = new Msg_Log();
            _ml.setMsg_table(msgTable);
            _ml.setLog_table(logTable);
            _ml.setMsgid(ent.getString("msgid"));
            _ml.setDatabase(dbTarget);

            String code = "0000";
            String rawDt = "";
            String telecom = "4";

            if (ent.getString("message_type").equalsIgnoreCase("AT")) {
                code =ent.optString("code", "E999");
                rawDt = ent.optString("res_dt", "");
            } else {
                code =ent.optString("code", "E999");
                rawDt = ent.optString("remark2", "");

                String rawRemark1 = ent.optString("remark1", "");

                if (rawRemark1 != null && !rawRemark1.isEmpty()) {
                    String r1 = rawRemark1.trim();

                    if (r1.equalsIgnoreCase("LGT") || r1.equals("019") || r1.equals("3")) {
                        telecom = "3";
                    } else if (r1.equalsIgnoreCase("SKT") || r1.equals("011") || r1.equals("1")) {
                        telecom = "1";
                    } else if (r1.equalsIgnoreCase("KTF") || r1.equalsIgnoreCase("KT") || r1.equals("016") || r1.equals("2")) {
                        telecom = "2";
                    } else {
                        telecom = "4";
                    }
                }
            }

            _ml.setCode(code);
            _ml.setTelecom(telecom);

            String cleanDt = rawDt.replaceAll("[^0-9]", ""); // 하이픈, 띄어쓰기, 콜론 싹 다 제거!

            if (cleanDt.length() > 14) {
                cleanDt = cleanDt.substring(0, 14);
            }

            _ml.setResult_dt(cleanDt);

            try {
                // 트랜잭션 (Update -> Insert Select -> Delete) 슛!
                requestService.applyResultProcess(_ml);
            } catch (Exception e) {
                log.error("[CMS] 결과 처리 업데이트 오류 [ {} ] - {}", _ml.getMsgid(), e.getMessage());
            }
        }
        log.info("[CMS] 결과 처리 완료 [ {} ] 건", json.length());
    }

    // 부모 추상 메서드 구현 (modId, dual 제거 완료!)
    @Override protected String getChannelName() { return "CMS"; }
    @Override protected String getDbTarget() { return this.dbTarget; }
    @Override protected String getDhnServer() { return this.dhnServer; }
    @Override protected String getUserid() { return this.userid; }
    @Override protected String getMsgTable() { return this.msgTable; }
    @Override protected String getLogTable() { return this.logTable; }
}