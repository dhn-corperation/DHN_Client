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

    private final Map<String, String> _msgCode = new HashMap<>();
    private final Map<String, String> _kaoCode = new HashMap<>();

    @PostConstruct
    public void init() {
        if ("Y".equalsIgnoreCase(cmsUse)) {
            // 코드 맵 초기화
            initCodeMap();
            super.initAgent();
        }
    }

    private void initCodeMap() {
        // CMS 코드 맵핑 초기화
        _kaoCode.put("0000","0000"); _kaoCode.put("3000","2001"); _kaoCode.put("1006","3005");
        _kaoCode.put("1001","3023"); _kaoCode.put("1003","3024"); _kaoCode.put("3012","3030");
        _kaoCode.put("3013","3031"); _kaoCode.put("3014","3032"); _kaoCode.put("3015","3033");
        _kaoCode.put("3016","3034"); _kaoCode.put("1002","3040"); _kaoCode.put("1004","3041");
        _kaoCode.put("1007","3044"); _kaoCode.put("1011","3048"); _kaoCode.put("3006","3049");
        _kaoCode.put("3019","3050"); _kaoCode.put("3005","3060"); _kaoCode.put("1012","3062");
        _kaoCode.put("1030","3063"); _kaoCode.put("9998","9998"); _kaoCode.put("9999","9999");
        _kaoCode.put("3008","1002"); _kaoCode.put("3018","E999");

        _msgCode.put("0000","0000"); _msgCode.put("7003","2100"); _msgCode.put("7050","2101");
        _msgCode.put("7028","2103"); _msgCode.put("7060","2104"); _msgCode.put("7087","2106");
        _msgCode.put("7086","2107"); _msgCode.put("7022","232");  _msgCode.put("7001","233");
        _msgCode.put("7095","249");  _msgCode.put("7093","250");  _msgCode.put("7061","263");
        _msgCode.put("7055","408");  _msgCode.put("7015","101");  _msgCode.put("7013","102");
        _msgCode.put("7014","103");  _msgCode.put("7056","108");  _msgCode.put("7057","112");
        _msgCode.put("7084","113");  _msgCode.put("7053","114");  _msgCode.put("7088","115");
        _msgCode.put("7051","116");  _msgCode.put("7023","201");  _msgCode.put("7008","204");
        _msgCode.put("7009","205");  _msgCode.put("7010","206");  _msgCode.put("7005","213");
        _msgCode.put("7076","216");  _msgCode.put("7098","39");   _msgCode.put("7099","1");
        _msgCode.put("7078","21");   _msgCode.put("7075","94");   _msgCode.put("7096","4008");
        _msgCode.put("7074","4306"); _msgCode.put("7021","4307"); _msgCode.put("7029","5300");
        _msgCode.put("7011","8011");

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

    // ⭐️ 결과 데이터 파싱 및 DB 업데이트 로직 (날짜 14자리 컷 적용 완료!)
    @Override
    protected void resultProc(JSONArray json) {
        log.info("[CMS] 결과 처리 시작 [ {} ] 건", json.length());

        for (int i = 0; i < json.length(); i++) {
            JSONObject ent = json.getJSONObject(i);

            Msg_Log _ml = new Msg_Log();
            _ml.setMsg_table(msgTable);
            _ml.setMsgid(ent.getString("msgid"));
            _ml.setDatabase(dbTarget);

            String code = "0000";
            String rawDt = ""; // 통신사가 준 날것의 19자리 시간 문자열

            // 1. 채널별 데이터 추출
            if (ent.getString("message_type").equalsIgnoreCase("AT")) {
                code = _kaoCode.getOrDefault(ent.getString("code"), "E999");
                rawDt = ent.optString("res_dt", "");
                _ml.setResult_message(ent.optString("message", ""));
                _ml.setReal_send_type("K1");
            } else {
                code = _msgCode.getOrDefault(ent.getString("code"), "8011");
                rawDt = ent.optString("remark2", "");
                _ml.setResult_message(ent.optString("message", ""));
            }

            _ml.setCode(code);

            // =====================================================================
            // 🚀 핵심 수정: 19자리 문자열("2026-07-31 08:58:40")을 14자리 숫자로 압축!
            // =====================================================================
            String cleanDt = rawDt.replaceAll("[^0-9]", ""); // 하이픈, 띄어쓰기, 콜론 싹 다 제거!

            // 만약 밀리초까지 줘서 14자리가 넘어가면 딱 14자리까지만 컷!
            if (cleanDt.length() > 14) {
                cleanDt = cleanDt.substring(0, 14);
            }

            // ⭐️ 오라클 에러(ORA-12899) 방지! 정제된 14자리 문자열 세팅!
            _ml.setResult_dt(cleanDt);

            // 월별 로그 테이블(YYYYMM) 파티셔닝 계산
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

            _ml.setLog_table(logTable + "_" + yyyymm);
            // =====================================================================

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