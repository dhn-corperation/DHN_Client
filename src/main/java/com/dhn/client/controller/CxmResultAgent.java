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

@Component
@Slf4j
public class CxmResultAgent extends AbstractResultAgent {

    @Autowired
    @Qualifier("cxmService")
    protected RequestService requestService;

    @Value("${dhnclient.cxm_use:N}") private String cxmUse;
    @Value("${dhnclient.cxm.userid:}") private String userid;
    @Value("${dhnclient.server:}") private String dhnServer;
    @Value("${dhnclient.cxm.db-target:oracle1}") private String dbTarget;
    @Value("${dhnclient.cxm.msg_table:EMFO_DATA}") private String msgTable;
    @Value("${dhnclient.cxm.log_table:EMFO_LOG}") private String logTable;

    @PostConstruct
    public void init() {
        if ("Y".equalsIgnoreCase(cxmUse)) {
            super.initAgent();
        }
    }

    @Scheduled(fixedDelay = 100)
    public void runResultProcess() {
        if (!"Y".equalsIgnoreCase(cxmUse)) return; // ⭐️ 스위치 방어막

        DbContextHolder.setDbTarget(dbTarget);
        try {
            super.executeResultProcess();
        } finally {
            DbContextHolder.clear();
        }
    }

    @Override
    protected void resultProc(JSONArray json) {
        log.info("[CXM] 결과 처리 시작 [ {} ] 건", json.length());

        for (int i = 0; i < json.length(); i++) {
            JSONObject ent = json.getJSONObject(i);

            Msg_Log _ml = new Msg_Log();
            _ml.setMsg_table(msgTable);
            _ml.setMsgid(ent.getString("msgid"));
            _ml.setDatabase(dbTarget);

            // =========================================================
            // 🚀 TODO: CXM 결과코드 ➔ 내부 매핑 구간
            // (명세서의 0:성공 / 카카오 7000:성공 여부에 따라 분기)
            // =========================================================
            String rawCode = ent.optString("code", "E999");
            _ml.setCode(rawCode);

            // 성공/실패 상태값 (CUR_STATE: 2=성공, 4=실패)
            if ("0000".equals(rawCode) || "7000".equals(rawCode) || "0".equals(rawCode)) {
                _ml.setStatus("2"); // 성공
            } else {
                _ml.setStatus("4"); // 실패
            }
            // =========================================================

            // 날짜 14자리 컷 및 로그테이블 YYYYMM 파티셔닝
            String rawDt = ent.optString("res_dt", "");
            String cleanDt = rawDt.replaceAll("[^0-9]", "");
            if (cleanDt.length() > 14) cleanDt = cleanDt.substring(0, 14);
            _ml.setResult_dt(cleanDt);
            _ml.setResult_message(ent.optString("message", ""));

            String yyyymm = "";
            try {
                if (cleanDt.length() >= 6) yyyymm = cleanDt.substring(0, 6);
                else yyyymm = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
            } catch (Exception e) {
                yyyymm = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
            }

            _ml.setLog_table(logTable + "_" + yyyymm);

            try {
                requestService.applyResultProcess(_ml);
            } catch (Exception e) {
                log.error("[CXM] 결과 처리 업데이트 오류 [ {} ] - {}", _ml.getMsgid(), e.getMessage());
            }
        }
        log.info("[CXM] 결과 처리 완료 [ {} ] 건", json.length());
    }

    @Override protected String getChannelName() { return "CXM"; }
    @Override protected String getDbTarget() { return this.dbTarget; }
    @Override protected String getDhnServer() { return this.dhnServer; }
    @Override protected String getUserid() { return this.userid; }
    @Override protected String getMsgTable() { return this.msgTable; }
    @Override protected String getLogTable() { return this.logTable; }
}