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
    @Value("${dhnclient.rms.msg_table:MTMSG_DATA}") private String msgTable;
    @Value("${dhnclient.rms.log_table:MTMSG_LOG}") private String logTable;

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

            Msg_Log ml = new Msg_Log();
            ml.setMsg_table(msgTable);
            ml.setLog_table(logTable);
            ml.setDatabase(dbTarget);
            ml.setMsgid(ent.getString("msgid"));

            String rawCode = ent.optString("code", "9999");
            String rawSCode = ent.optString("s_code", "");
            String rawRemark1 = ent.optString("remark1", "");
            String recvTimeRaw = ent.optString("remark2", "");

            if (recvTimeRaw.trim().isEmpty()) {
                recvTimeRaw = ent.optString("res_dt", "");
            }

            String cleanCode = rawCode.replaceAll("[^0-9]", "");
            if(cleanCode.isEmpty()) cleanCode = "";

            String cleanSCode = rawSCode.replaceAll("[^0-9]", "");
            if(cleanSCode.isEmpty()) cleanSCode = "";

            String telecomMapped = "";
            String pre_telecomMapped = "";

            String message_type = ent.optString("message_type","");
            String rslt_type = "";
            String rslt_code = "";
            String pre_rslt_type = "";
            String pre_rslt_code = "";

            if("PH".equalsIgnoreCase(message_type)){

                rslt_code = cleanCode;

                if(!cleanSCode.trim().isEmpty()) {
                    pre_rslt_code = cleanCode;
                    rslt_code = cleanSCode;
                    if("S".equalsIgnoreCase(ent.optString("sms_kind", ""))){
                        pre_rslt_type = "SMS";
                    }else{
                        pre_rslt_type = "LMS";
                    }

                    if (rawRemark1 != null && !rawRemark1.trim().isEmpty()) {
                        String r1 = rawRemark1.trim();
                        if (r1.equalsIgnoreCase("LGT") || r1.equals("019") || r1.equals("3")) {
                            pre_telecomMapped = "LGT";
                        } else if (r1.equalsIgnoreCase("SKT") || r1.equals("011") || r1.equals("1")) {
                            pre_telecomMapped = "SKT";
                        } else if (r1.equalsIgnoreCase("KTF") || r1.equalsIgnoreCase("KT") || r1.equals("016") || r1.equals("2")) {
                            pre_telecomMapped = "KTF";
                        } else {
                            pre_telecomMapped = "ETC";
                        }
                    }
                } else {
                    if("S".equalsIgnoreCase(ent.optString("sms_kind", ""))){
                        rslt_type = "SMS";
                    }else{
                        rslt_type = "LMS";
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
            ml.setPre_media_type(pre_rslt_type);
            ml.setS_code(pre_rslt_code);
            ml.setStatus("6");
            ml.setTelecom(telecomMapped);
            ml.setPre_telecom(pre_telecomMapped);
            ml.setResult_dt(recvTimeRaw);

            try {
                requestService.applyResultProcess(ml);
            } catch (Exception e) {
                log.error("[RMS] 결과 처리 업데이트 오류 [ {} ] - {}", ml.getMsgid(), e.getMessage());
            }
        }
        log.info("[RMS] 결과 처리 완료 [ {} ] 건", json.length());
    }

    @Override protected String getChannelName() { return "RMS"; }
    @Override protected String getDbTarget() { return this.dbTarget; }
    @Override protected String getDhnServer() { return this.dhnServer; }
    @Override protected String getUserid() { return this.userid; }
}