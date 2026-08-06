package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CxmSendAgent extends AbstractSendAgent {

    @Autowired
    @Qualifier("cxmService")
    private RequestService requestService;

    // ⭐️ CXM 마스터 스위치 및 yml 세팅
    @Value("${dhnclient.cxm_use:N}") private String cxmUse;
    @Value("${dhnclient.cxm.userid:}") private String userid;
    @Value("${dhnclient.server:}") private String dhnServer;
    @Value("${dhnclient.cxm.db-target:oracle}") private String dbTarget;
    @Value("${dhnclient.cxm.msg_table:EMFO_DATA}") private String msgTable;
    @Value("${dhnclient.cxm.log_table:EMFO_LOG}") private String logTable;

    // ⭐️ 수신거부 번호 (yml에서 주입, 없으면 기본값)
    @Value("${dhnclient.block_sender:080-1234-5678}") private String blockSender;

    // ⏰ CXM 채널: 알림톡(AT), 친구톡(FT), LMS 순회!
    @Scheduled(fixedDelay = 1000)
    public void SendProcess() {
        if (!"Y".equalsIgnoreCase(cxmUse)) return; // 스위치 On일 때만 동작

        String[] msgTypes = {"AT", "AI", "FT", "FI", "LMS", "SMS"};
        for (String msgType : msgTypes) {
            super.executeProcess(this.dhnServer, this.userid, msgType);
        }
    }

    @Override protected String getChannelName() { return "CXM"; }
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
                if (bean.getPhn() == null || bean.getPhn().length() < 10) {
                    invalidList.add(bean.getMsgid());
                    continue;
                }

                // =========================================================
                // 🚀 [특이사항] 광고문자(LMS + ETC5='Y') 본문 강제 조립 로직
                // =========================================================
                if ("LMS".equalsIgnoreCase(msgType)) {

                    if("Y".equalsIgnoreCase(bean.getEtc5())){
                        String originalMsg = bean.getMsg();
                        // 프리픽스와 서픽스 조립
                        String adMsg = "(광고) 강원랜드\n" + originalMsg + "\n무료수신거부: " + blockSender;
                        bean.setMsg(adMsg);
                        bean.setMsgsms(adMsg);
                    }

                    // 메시지 타입 셋팅
                    bean.setMessagetype("PH");
                    bean.setSmskind("L");
                } else if ("SMS".equalsIgnoreCase(msgType)) {
                    if("Y".equalsIgnoreCase(bean.getEtc5())){
                        String originalMsg = bean.getMsg();
                        // 프리픽스와 서픽스 조립
                        String adMsg = "(광고) 강원랜드\n" + originalMsg + "\n무료수신거부: " + blockSender;
                        bean.setMsg(adMsg);
                        bean.setMsgsms(adMsg);
                    }

                    // 메시지 타입 셋팅
                    bean.setMessagetype("PH");
                    bean.setSmskind("S");
                } else if ("AT".equalsIgnoreCase(msgType) || "AI".equalsIgnoreCase(msgType)) {
                    parseRmsButton(bean, mapper);
                    bean.setMessagetype("AT");
                    try {
                        byte[] msgBytes = bean.getMsg() != null ? bean.getMsg().getBytes("EUC-KR") : new byte[0];
                        if (msgBytes.length > 90) {
                            bean.setSmskind("L");
                        } else {
                            bean.setSmskind("S");
                        }
                    } catch (Exception e) {
                        bean.setSmskind("L"); // 예외 시 기본 단문 처리
                    }
                }else if ("FT".equalsIgnoreCase(msgType) || "FI".equalsIgnoreCase(msgType)) {

                    // 1. 타입에 맞게 E1, E2 세팅
                    if ("FT".equalsIgnoreCase(msgType)) {
                        bean.setMessagetype("E1");
                    } else if("FI".equalsIgnoreCase(msgType)){
                        bean.setMessagetype("E2");
                    }

                    // 2. ⭐️ [신규] 이미지 + 버튼 통합 파싱 태우기!
                    parseCxmAttachment(bean, mapper);

                    // 3. SMS/LMS 우회 발송 (대체문자) 길이 체크
//                    try {
//                        byte[] msgBytes = bean.getMsg() != null ? bean.getMsg().getBytes("EUC-KR") : new byte[0];
//                        if (msgBytes.length > 90) {
//                            bean.setSmskind("L");
//                        } else {
//                            bean.setSmskind("S");
//                        }
//                    } catch (Exception e) {
//                        bean.setSmskind("L"); // 예외 시 기본 단문(LMS) 처리
//                    }
                }
                // =========================================================

                boolean isGoodData = bean.processJsonPayload(mapper, invalidList);
                if (isGoodData) {
                    finalSendList.add(bean);
                }
            }

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
            log.error("[CXM - {}] 데이터 조회/정제 오류: {}", msgType, e.getMessage());
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
            log.error("[CXM] 상태값 업데이트 오류: {}", e.getMessage());
        }
    }

    private void parseRmsButton(RequestBean bean, ObjectMapper mapper) {
        String rawButton = bean.getButton(); // 쿼리에서 BUTTON_URL AS button 으로 가져온 값

        if (rawButton == null || rawButton.trim().isEmpty()) {
            return;
        }

        try {
            // 1. 파이프(|) 기호로 여러 개의 버튼을 배열로 분리 (정규식 예약어라 \\| 사용)
            String[] btnArray = rawButton.split("\\|");

            // 최대 5개까지만 처리
            for (int i = 0; i < btnArray.length; i++) {
                if (i >= 5) break;

                // 2. 캐럿(^) 기호로 버튼 내부 속성을 분리 (배열 길이 유지를 위해 -1 옵션 추가)
                String[] btnInfo = btnArray[i].split("\\^", -1);

                // 최소한 이름과 타입은 있어야 함
                if (btnInfo.length >= 2) {
                    // 3. Jackson ObjectNode를 이용해 깔끔한 JSON 객체 생성
                    ObjectNode btnNode = mapper.createObjectNode();
                    btnNode.put("name", btnInfo[0].trim()); // 버튼명
                    btnNode.put("type", btnInfo[1].trim()); // 버튼타입 (WL, AL 등)

                    // 4. 모바일 URL이 존재하면 세팅
                    if (btnInfo.length >= 3 && !btnInfo[2].trim().isEmpty()) {
                        btnNode.put("url_mobile", btnInfo[2].trim());
                    }

                    // 5. PC URL이 존재하면 세팅
                    if (btnInfo.length >= 4 && !btnInfo[3].trim().isEmpty()) {
                        btnNode.put("url_pc", btnInfo[3].trim());
                    }

                    // 6. 완성된 JSON 객체를 문자열로 변환
                    String singleBtnJson = mapper.writeValueAsString(btnNode);

                    // 7. 순서대로 button1 ~ button5 필드에 장전!
                    switch (i) {
                        case 0: bean.setButton1(singleBtnJson); break;
                        case 1: bean.setButton2(singleBtnJson); break;
                        case 2: bean.setButton3(singleBtnJson); break;
                        case 3: bean.setButton4(singleBtnJson); break;
                        case 4: bean.setButton5(singleBtnJson); break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("[RMS] 버튼 파싱 실패 (msgid: {}): {}", bean.getMsgid(), e.getMessage());
        }
    }

    private void parseCxmAttachment(RequestBean bean, ObjectMapper mapper) {
        try {
            ObjectNode attachmentNode = mapper.createObjectNode();

            // ==========================================
            // 1. 이미지 처리 (FI 일 때 주로 들어옴)
            // ==========================================
            // 🚨 주의: DB INSERT문 기준 FT_IMG_PATH(imagelink)가 이미지 소스, FT_IMG_URL(imageurl)이 클릭 랜딩 링크입니다!
            String imgPath = bean.getImagelink(); // 카카오 이미지 URL (mud-kage...)
            String imgUrl = bean.getImageurl();   // 랜딩 URL (high1.com...)

            if (imgPath != null && !imgPath.trim().isEmpty()) {
                ObjectNode imageNode = mapper.createObjectNode();
                imageNode.put("img_url", imgPath.trim());

                if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                    imageNode.put("img_link", imgUrl.trim());
                } else {
                    imageNode.put("img_link", ""); // 이미지가 있으면 보통 링크도 필수이므로 빈값이라도 방어
                }

                // attachment 노드 안에 "image" 객체 꽂기
                attachmentNode.set("image", imageNode);
            }

            // ==========================================
            // 2. 버튼 처리 (FT, FI 공통) - 파이프(|) 와 캐럿(^) 분리
            // ==========================================
            String rawButton = bean.getButton();
            if (rawButton != null && !rawButton.trim().isEmpty()) {
                ArrayNode buttonArray = mapper.createArrayNode();
                String[] btnArray = rawButton.split("\\|");

                for (String btnStr : btnArray) {
                    String[] btnInfo = btnStr.split("\\^", -1);
                    if (btnInfo.length >= 2) {
                        ObjectNode btnNode = mapper.createObjectNode();
                        btnNode.put("name", btnInfo[0].trim());
                        btnNode.put("type", btnInfo[1].trim());

                        if (btnInfo.length >= 3 && !btnInfo[2].trim().isEmpty()) {
                            btnNode.put("url_mobile", btnInfo[2].trim());
                        }
                        if (btnInfo.length >= 4 && !btnInfo[3].trim().isEmpty()) {
                            btnNode.put("url_pc", btnInfo[3].trim());
                        }
                        // Array에 버튼 객체 추가
                        buttonArray.add(btnNode);
                    }
                }

                // 버튼이 1개라도 파싱되었다면 attachment 노드 안에 "button" 배열 꽂기
                if (buttonArray.size() > 0) {
                    attachmentNode.set("button", buttonArray);
                }
            }

            // ==========================================
            // 3. 최종 완성된 JSON을 Bean에 세팅
            // ==========================================
            if (!attachmentNode.isEmpty()) {
                String finalAttachmentJson = mapper.writeValueAsString(attachmentNode);
                bean.setAttachments(finalAttachmentJson); // ⭐️ (주의) 형님 Bean에 setAttachment 인지 setAttachments 인지 확인 필요!

//                log.info("[CXM] 친구톡 첨부 생성 완료 (MSGID: {}): {}", bean.getMsgid(), finalAttachmentJson);
            }

        } catch (Exception e) {
            log.error("[CXM] 친구톡 attachment 파싱 실패 (msgid: {}): {}", bean.getMsgid(), e.getMessage());
        }
    }
}