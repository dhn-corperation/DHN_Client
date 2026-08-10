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
public class WebResultAgent extends AbstractResultAgent {

    @Autowired
    @Qualifier("webService")
    protected RequestService requestService;

    @Value("${dhnclient.web_use:N}") private String webUse;
    @Value("${dhnclient.web.userid:}") private String userid;
    @Value("${dhnclient.server:}") private String dhnServer;
    @Value("${dhnclient.web.db-target:mssql}") private String dbTarget;
    @Value("${dhnclient.web.msg_table:SUREDATA}") private String msgTable;
    @Value("${dhnclient.web.log_table:SUREDATA_LOG}") private String logTable;
    @Value("${dhnclient.web.log_back:N}") private String webLogBack;

    @PostConstruct
    public void init() {
        if ("Y".equalsIgnoreCase(webUse)) {
            super.initAgent();
        }
    }

    @Scheduled(fixedDelay = 100)
    public void runResultProcess() {
        if (!"Y".equalsIgnoreCase(webUse)) return;

        DbContextHolder.setDbTarget(dbTarget);
        try {
            super.executeResultProcess();
        } finally {
            DbContextHolder.clear();
        }
    }

    @Override
    protected void resultProc(JSONArray json) {
        log.info("[WEB] 결과 처리 시작 [ {} ] 건", json.length());

        for (int i = 0; i < json.length(); i++) {
            JSONObject ent = json.getJSONObject(i);

            String resultLogTable = logTable;

            Msg_Log _ml = new Msg_Log();
            _ml.setMsg_table(msgTable);
            _ml.setMsgid(ent.getString("msgid"));
            _ml.setDatabase(dbTarget);

            // =========================================================
            // 💡 JSON 필드를 Msg_Log 객체에 매핑
            // =========================================================
            String rawCode = ent.optString("code", "9999"); // 최종 결과코드
            String rawSCode = ent.optString("s_code", ""); // 알림톡 1차 결과코드
            String rawRemark1 = ent.optString("remark1", "");
            String recvTimeRaw = ent.optString("res_dt", ent.optString("remark2", "")); // 수신시간
            String rawKind = "";

            // MS-SQL INT 타입 에러 방지를 위해 숫자만 추출
            String cleanCode = rawCode.replaceAll("[^0-9]", "");
            if(cleanCode.isEmpty()) cleanCode = "9999";

            String cleanSCode = rawSCode.replaceAll("[^0-9]", "");
            String telecom = ""; // 기본값 (ETC 등)

            if(cleanSCode.equals("0000")){

            }else{

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

            // 수신시간 14자리 정제
            String cleanDt = recvTimeRaw.replaceAll("[^0-9]", "");
            if (cleanDt.length() > 14) cleanDt = cleanDt.substring(0, 14);
            _ml.setResult_dt(cleanDt);

            _ml.setResult_message(ent.optString("message", ""));

            if("Y".equalsIgnoreCase(webLogBack)){
                String yyyymm = "";
                try {
                    if (cleanDt.length() >= 6) {
                        yyyymm = cleanDt.substring(0, 6); // 14자리 중 맨 앞 6자리가 YYYYMM
                    }

                    if (yyyymm.isEmpty()) {
                        yyyymm = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
                    }
                } catch (Exception e) {
                    yyyymm = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
                }
                resultLogTable += "_" + yyyymm;
            }

            _ml.setLog_table(resultLogTable);


            try {
                requestService.applyResultProcess(_ml);
            } catch (Exception e) {
                log.error("[WEB] 결과 처리 업데이트 오류 [ {} ] - {}", _ml.getMsgid(), e.getMessage());
            }
        }
        log.info("[WEB] 결과 처리 완료 [ {} ] 건", json.length());
    }

    // =========================================================
    // ⭐️ IDE 에러를 잠재우는 필수 추상 메서드 오버라이드 6형제!
    // =========================================================
    @Override protected String getChannelName() { return "WEB"; }
    @Override protected String getDbTarget() { return this.dbTarget; }
    @Override protected String getDhnServer() { return this.dhnServer; }
    @Override protected String getUserid() { return this.userid; }
    @Override protected String getMsgTable() { return this.msgTable; }
    @Override protected String getLogTable() { return this.logTable; }
}