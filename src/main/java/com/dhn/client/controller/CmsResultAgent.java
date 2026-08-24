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
public class CmsResultAgent extends AbstractResultAgent {

    @Autowired
    @Qualifier("cmsService")
    protected RequestService requestService;

    @Value("${dhnclient.cms_use:N}") private String cmsUse;
    @Value("${dhnclient.cms.userid:}") private String userid;
    @Value("${dhnclient.server:}") private String dhnServer;
    @Value("${dhnclient.cms.db-target:mssql}") private String dbTarget;
    @Value("${dhnclient.cms.msg_table:MTMSG_DATA}") private String msgTable;
    @Value("${dhnclient.cms.log_table:MTMSG_LOG}") private String logTable;

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

            Msg_Log ml = new Msg_Log();
            ml.setMsg_table(msgTable);
            ml.setLog_table(logTable);
            ml.setDatabase(dbTarget); // SQL 빈값 방지
            ml.setMsgid(ent.getString("msgid"));

            // API에서 던져주는 결과 필드 추출
            String rawCode = ent.optString("code", "9999");
            String rawSCode = ent.optString("s_code", "");
            String rawRemark1 = ent.optString("remark1", ""); // 통신사 정보
            String recvTimeRaw = ent.optString("remark2", ent.optString("res_dt", ""));

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
                log.error("[CMS] 결과 처리 업데이트 오류 [ {} ] - {}", ml.getMsgid(), e.getMessage());
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