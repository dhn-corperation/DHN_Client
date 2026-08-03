package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class RmsSendAgent extends AbstractSendAgent {

    @Autowired
    @Qualifier("rmsService") // ⭐️ RMS 전용 서비스 빈!
    private RequestService requestService;

    // yml 설정값 바인딩
    @Value("${dhnclient.rms.userid:}") private String userid;
    @Value("${dhnclient.server:}") private String dhnServer;
    @Value("${dhnclient.rms.db-target:oracle}") private String dbTarget;
    @Value("${dhnclient.rms.msg_table:SUREDATA}") private String msgTable;
    @Value("${dhnclient.rms.log_table:SUREDATA_LOG}") private String logTable;
    @Value("${dhnclient.rms_use:N}") private String rmsUse;

    // ⏰ RMS 채널은 알림톡(at), MMS(mms) 순회!
    @Scheduled(fixedDelay = 1000)
    public void SendProcess() {
        if (!"Y".equalsIgnoreCase(rmsUse)) {
            return;
        }
        String[] msgTypes = {"at", "mms"};
        for (String msgType : msgTypes) {
            super.executeProcess(this.dhnServer, this.userid, msgType);
        }
    }

    @Override protected String getChannelName() { return "RMS"; }
    @Override protected String getDbTarget() { return this.dbTarget; }

    @Override
    protected List<RequestBean> fetchWaitingData(String msgType) {
        List<RequestBean> finalSendList = new ArrayList<>();
        List<String> invalidList = new ArrayList<>();

        try {
            SQLParameter param = new SQLParameter();
            param.setMsg_table(msgTable);
            param.setMsg_type(msgType);
            param.setDatabase(dbTarget);

            List<RequestBean> rawList = requestService.selectRequests(param);
            if (rawList == null || rawList.isEmpty()) return finalSendList;

            ObjectMapper mapper = new ObjectMapper();

            for (RequestBean bean : rawList) {
                // [검증] 번호 길이 컷!
                if (bean.getPhn() == null || bean.getPhn().length() < 10) {
                    invalidList.add(bean.getMsgid());
                    continue;
                }

                // =========================================================
                // 📸 ⭐️ [특이사항] MMS 이미지 선(先) 업로드 및 ID 매핑 ⭐️
                // =========================================================
                if ("mms".equalsIgnoreCase(msgType)) {
                    String imagePath = bean.getAttimage(); // DB에 적재된 이미지 절대경로
                    if (imagePath != null && !imagePath.isEmpty()) {

                        // TODO: 형님이 주실 API 명세로 이미지 업로드 모듈 짜서 넣을 곳!
                        // String imageId = imageUploadService.uploadAndGetId(imagePath);
                        String imageId = "TEMP_IMG_ID_12345"; // 임시 더미 ID

                        if(imageId != null) {
                            bean.setMmsimageid(imageId); // ⭐️ 리퀘스트 필드에 장전 완료!
                        } else {
                            log.error("[RMS] 이미지 업로드 실패로 인한 발송 컷! MSGID: {}", bean.getMsgid());
                            invalidList.add(bean.getMsgid());
                            continue;
                        }
                    }
                }
                // =========================================================

                // 공통 JSON 정제
                boolean isGoodData = bean.processJsonPayload(mapper, invalidList);
                if (isGoodData) {
                    finalSendList.add(bean);
                }
            }

            // 불량 데이터 처리
            if (!invalidList.isEmpty()) {
                Msg_Log ml = new Msg_Log();
                ml.setMsg_table(msgTable);
                ml.setLog_table(logTable);
                ml.setStatus("4");
                ml.setCode("7999");
                ml.setDatabase(dbTarget);
                requestService.updateInvalidData(invalidList, ml);
            }
        } catch (Exception e) {
            log.error("[RMS - {}] 데이터 조회/정제 오류: {}", msgType, e.getMessage());
        }
        return finalSendList;
    }

    @Override
    protected void updateStatusToSent(List<String> msgIds) {
        try {
            SQLParameter param = new SQLParameter();
            param.setMsg_table(msgTable);
            param.setMsgIds(msgIds);
            param.setDatabase(dbTarget);
            requestService.updateSendComplete(param);
        } catch (Exception e) {
            log.error("[RMS] 상태값 업데이트 오류: {}", e.getMessage());
        }
    }
}