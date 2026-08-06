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
    @Value("${dhnclient.cxm.db-target:oracle}") private String dbTarget;
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
        if (!"Y".equalsIgnoreCase(cxmUse)) return;

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
            _ml.setLog_table(logTable);
            _ml.setMsgid(ent.getString("msgid"));
            _ml.setDatabase(dbTarget);

            String rawCode = ent.optString("code", "9999"); // 최종 결과코드
            String rawSCode = ent.optString("s_code", ""); // 알림톡 1차 결과코드
            String rawRemark1 = ent.optString("remark1", "");
            String recvTimeRaw = ent.optString("res_dt", ent.optString("remark2", "")); // 수신시간
            String rawKind = "";

            String cleanCode = rawCode.replaceAll("[^0-9]", "");
            if(cleanCode.isEmpty()) cleanCode = "9999";

            String cleanSCode = rawSCode.replaceAll("[^0-9]", "");
            String telecom = ""; // 기본값 (ETC 등)

            if(cleanSCode.equals("0000")){
                cleanCode = "0000";
            }else{

                if(cleanSCode.isEmpty()){
                    cleanSCode = cleanCode;
                    cleanCode = "";
                }

                if (rawRemark1 != null && !rawRemark1.isEmpty()) {
                    String r1 = rawRemark1.trim();

                    if (r1.equalsIgnoreCase("LGT") || r1.equals("019") || r1.equals("3")) {
                        telecom = "3"; // LGT 계열 코드 (숫자형으로 매핑)
                    } else if (r1.equalsIgnoreCase("SKT") || r1.equals("011") || r1.equals("1")) {
                        telecom = "1"; // SKT 계열 코드
                    } else if (r1.equalsIgnoreCase("KTF") || r1.equalsIgnoreCase("KT") || r1.equals("016") || r1.equals("2")) {
                        telecom = "2"; // KT 계열 코드
                    } else {
                        telecom = "4"; // SKT 또는 미지정 등 시스템별 0번 코드
                    }
                }

                rawKind = ent.optString("sms_kind", "");
            }

            String cleanTelecom = telecom.replaceAll("[^0-9]", "");

            // 최종 결과코드(code) 기준으로 상태값 판별
            if ("7000".equals(cleanCode) || "0000".equals(cleanCode)) {
                _ml.setStatus("2"); // 성공
            } else {
                _ml.setStatus("4"); // 실패
            }

            _ml.setCode(cleanCode);       // 최종코드 세팅
            _ml.setS_code(cleanSCode);    // 카톡코드 세팅
            _ml.setTelecom(cleanTelecom); // 통신사 세팅
            _ml.setReal_send_type(rawKind); // 문자 타입 세팅
            // =========================================================

            // 날짜 14자리 압축 및 YYYYMM 파티셔닝
            String rawDt = ent.optString("res_dt", "");
            String cleanDt = rawDt.replaceAll("[^0-9]", "");
            if (cleanDt.length() > 14) cleanDt = cleanDt.substring(0, 14);

            _ml.setResult_dt(cleanDt);
            _ml.setResult_message(ent.optString("message", ""));


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