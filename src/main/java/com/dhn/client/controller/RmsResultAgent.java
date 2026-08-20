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

    @Value("${dhnclient.rms_use:N}") private String rmsUse;
    @Value("${dhnclient.rms.userid:}") private String userid;
    @Value("${dhnclient.server:}") private String dhnServer;
    @Value("${dhnclient.rms.db-target:mssql}") private String dbTarget;
    @Value("${dhnclient.rms.msg_table:SUREDATA}") private String msgTable;
    @Value("${dhnclient.rms.log_table:SUREDATA_LOG}") private String logTable;
    @Value("${dhnclient.rms.log_back:N}") private String rmsLogBack;

    @PostConstruct
    public void init() {
        if ("Y".equalsIgnoreCase(rmsUse)) {
            super.initAgent();
        }
    }

    @Scheduled(fixedDelay = 100)
    public void runResultProcess() {
        if (!"Y".equalsIgnoreCase(rmsUse)) {
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
        log.info("[RMS] 결과 처리 시작 [ {} ] 건", json.length());

        for (int i = 0; i < json.length(); i++) {
            JSONObject ent = json.getJSONObject(i);

            String resultLogTable = logTable;

            Msg_Log _ml = new Msg_Log();
            _ml.setMsg_table(msgTable);
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
            String telecom = "4"; // 기본값 (ETC 등)
            String mediatype = "";

            if(cleanSCode.equals("0000")){
                cleanCode = "0000";
                telecom = "0000";
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
            if ("7000".equals(cleanSCode) || "0000".equals(cleanSCode) || "0000".equals(cleanCode)) {
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

            if("Y".equalsIgnoreCase(rmsLogBack)){
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