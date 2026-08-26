package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
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
public class ErpResultAgent extends AbstractResultAgent {

    // ⭐️ 앞서 만든 ERP 전용 서비스를 동일하게 주입받습니다.
    @Autowired
    @Qualifier("erpService")
    private RequestService requestService;

    // ⭐️ yml 설정값 매핑
    @Value("${dhnclient.erp.userid:}")
    private String userid;

    @Value("${dhnclient.server:}")
    private String dhnServer;

    @Value("${dhnclient.erp.db-target:mssql}")
    private String dbTarget;

    @Value("${dhnclient.erp.msg_table:TBL_ERP_QUEUE}")
    private String msgTable;

    @Value("${dhnclient.erp.log_table:TBL_ERP_QUEUE_LOG}")
    private String logTable;

    @Value("${dhnclient.erp_use:N}")
    private String erpUse;

    @PostConstruct
    public void init() {
        if ("Y".equalsIgnoreCase(erpUse)) {
            super.initAgent();
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void runResultProcess() {
        if (!"Y".equalsIgnoreCase(erpUse)) {
            return;
        }
        super.executeResultProcess();
    }
    @Override protected String getChannelName() { return "ERP"; }
    @Override protected String getDbTarget() { return this.dbTarget; }
    @Override protected String getUserid() { return this.userid; }
    @Override protected String getDhnServer() { return this.dhnServer; }
    @Override protected String getMsgTable() { return this.msgTable; }
    @Override protected String getLogTable() { return this.logTable; }
    @Override
    protected void resultProc(JSONArray json) {
        log.info("[ERP] 결과 처리 시작 [ {} ] 건", json.length());

        for (int i = 0; i < json.length(); i++) {
            JSONObject ent = json.getJSONObject(i);

            Msg_Log ml = new Msg_Log();
            ml.setMsg_table(msgTable);
            ml.setLog_table(logTable);
            ml.setDatabase(dbTarget); // SQL 빈값 방지
            ml.setMsgid(ent.getString("msgid"));

            // API에서 던져주는 결과 필드 추출
            String rawCode = ent.optString("code", "9999");
            String rawSCode = ent.optString("s_code", "");
            String rawRemark1 = ent.optString("remark1", ""); // 통신사 정보
            String recvTimeRaw = ent.optString("remark2", "");

            if (recvTimeRaw.trim().isEmpty()) {
                recvTimeRaw = ent.optString("res_dt", "");
            }

            // 코드 숫자만 남기기
            String cleanCode = rawCode.replaceAll("[^0-9]", "");
            if(cleanCode.isEmpty()) cleanCode = "";

            String cleanSCode = rawSCode.replaceAll("[^0-9]", "");
            if(cleanSCode.isEmpty()) cleanSCode = "";

            String telecomMapped = "";

            String message_type = ent.optString("message_type","");
            String rslt_type = "";
            String rslt_code = "";
            String pre_rslt_type = ""; // 재발송 전 타입은 모름
            String pre_rslt_code = "";

            if("PH".equalsIgnoreCase(message_type)){
                if("S".equalsIgnoreCase(ent.optString("sms_kind", ""))){
                    rslt_type = "SMS";
                }else{
                    rslt_type = "LMS";
                }

                rslt_code = cleanCode;

                if(!cleanSCode.trim().isEmpty()) {
                    pre_rslt_code = cleanCode;
                    rslt_code = cleanSCode;
                }

                if (rawRemark1 != null && !rawRemark1.trim().isEmpty()) {
                    String r1 = rawRemark1.trim();
                    if (r1.equalsIgnoreCase("LGT") || r1.equals("019") || r1.equals("3")) {
                        telecomMapped = "LGT";
                    } else if (r1.equalsIgnoreCase("SKT") || r1.equals("011") || r1.equals("1")) {
                        telecomMapped = "SKT";
                    } else if (r1.equalsIgnoreCase("KTF") || r1.equalsIgnoreCase("KT") || r1.equals("016") || r1.equals("2")) {
                        telecomMapped = "KTF";
                    } else {
                        telecomMapped = "ETC";
                    }
                }
            } else {
                if("AT".equalsIgnoreCase(message_type)){
                    rslt_type = "ALT";
                }else if ("AI".equalsIgnoreCase(message_type)){
                    rslt_type = "ALI";
                }else if (message_type.toUpperCase().startsWith("B") || message_type.toUpperCase().startsWith("E")){
                    rslt_type = "BRI";
                }
                rslt_code = cleanSCode;
            }

            ml.setCode(rslt_code);
            ml.setMedia_type(rslt_type);
            ml.setS_code(pre_rslt_code);
            ml.setStatus("6"); // 결과 상태값
            ml.setTelecom(telecomMapped);
            ml.setResult_dt(recvTimeRaw);

            try {
                requestService.applyResultProcess(ml);
            } catch (Exception e) {
                log.error("[ERP] 결과 처리 업데이트 오류 [ {} ] - {}", ml.getMsgid(), e.getMessage());
            }
        }
        log.info("[ERP] 결과 처리 완료 [ {} ] 건", json.length());
    }
}