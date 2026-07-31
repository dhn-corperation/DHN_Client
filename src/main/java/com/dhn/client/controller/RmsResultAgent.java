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
public class RmsResultAgent extends AbstractResultAgent {

    @Autowired
    @Qualifier("rmsService")
    protected RequestService requestService;

    @Value("${dhnclient.rms.userid:}") private String userid;
    @Value("${dhnclient.server:}") private String dhnServer;
    @Value("${dhnclient.rms.db-target:oracle1}") private String dbTarget;
    @Value("${dhnclient.rms.msg_table:SUREDATA}") private String msgTable;
    @Value("${dhnclient.rms.log_table:SUREDATA_LOG}") private String logTable;

    @PostConstruct
    public void init() {
        super.initAgent();
    }

    @Scheduled(fixedDelay = 100)
    public void runResultProcess() {
        DbContextHolder.setDbTarget(dbTarget);
        try {
            super.executeResultProcess();
        } finally {
            DbContextHolder.clear();
        }
    }

    @Override
    protected void resultProc(JSONArray json) {
        log.info("[RMS] 결과 처리 시작 [ {} ] 건", json.length());

        for (int i = 0; i < json.length(); i++) {
            JSONObject ent = json.getJSONObject(i);

            Msg_Log _ml = new Msg_Log();
            _ml.setMsg_table(msgTable);
            _ml.setMsgid(ent.getString("msgid"));
            _ml.setDatabase(dbTarget);

            // =========================================================
            // 🚀 TODO: RMS(슈어엠) 통신사 결과코드 ➔ 내부 에러코드 맵핑 구간!
            // =========================================================
            String rawCode = ent.optString("code", "E999"); // 통신사가 준 원본 코드

            // 나중에 맵핑 테이블(_rmsCodeMap)이 나오면 아래 주석을 풀고 적용하시면 됩니다.
            // String mappedCode = _rmsCodeMap.getOrDefault(rawCode, "E999");
            // _ml.setCode(mappedCode);

            // 일단은 맵핑 전이므로 원본 코드를 그대로 세팅합니다! (xml에서 ERRCODE로 들어감)
            _ml.setCode(rawCode);
            // =========================================================

            // 날짜 14자리 압축 및 YYYYMM 파티셔닝
            String rawDt = ent.optString("res_dt", "");
            String cleanDt = rawDt.replaceAll("[^0-9]", "");
            if (cleanDt.length() > 14) cleanDt = cleanDt.substring(0, 14);

            _ml.setResult_dt(cleanDt);
            _ml.setResult_message(ent.optString("message", ""));

            String yyyymm = "";
            try {
                if (cleanDt.length() >= 6) {
                    yyyymm = cleanDt.substring(0, 6);
                } else {
                    yyyymm = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
                }
            } catch (Exception e) {
                yyyymm = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
            }

            _ml.setLog_table(logTable + "_" + yyyymm);

            try {
                // UPDATE (ERRCODE 포함) -> INSERT LOG -> DELETE 트랜잭션 실행!
                requestService.applyResultProcess(_ml);
            } catch (Exception e) {
                log.error("[RMS] 결과 처리 업데이트 오류 [ {} ] - {}", _ml.getMsgid(), e.getMessage());
            }
        }
        log.info("[RMS] 결과 처리 완료 [ {} ] 건", json.length());
    }

    @Override protected String getChannelName() { return "RMS"; }
    @Override protected String getDbTarget() { return this.dbTarget; }
    @Override protected String getDhnServer() { return this.dhnServer; }
    @Override protected String getUserid() { return this.userid; }
    @Override protected String getMsgTable() { return this.msgTable; }
    @Override protected String getLogTable() { return this.logTable; }
}