package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WebSendAgent extends AbstractSendAgent {

    @Autowired
    @Qualifier("webService")
    private RequestService requestService;

    @Value("${dhnclient.web_use:N}") private String webUse;
    @Value("${dhnclient.web.userid:}") private String userid;
    @Value("${dhnclient.server:}") private String dhnServer;
    @Value("${dhnclient.web.db-target:mssql}") private String dbTarget;
    @Value("${dhnclient.web.msg_table:SUREDATA}") private String msgTable;
    @Value("${dhnclient.web.log_table:SUREDATA_LOG}") private String logTable;

    // ⏰ 알림톡(T) 타입 타겟으로 스케줄러 구동
    @Scheduled(fixedDelay = 1000)
    public void SendProcess() {
        if (!"Y".equalsIgnoreCase(webUse)) return;

        // DB의 KIND 컬럼이 'T' (카카오비즈메시지/알림톡)인 것을 타겟팅
        String[] msgTypes = {"S", "M", "T"};
        for (String msgType : msgTypes) {
            super.executeProcess(this.dhnServer, this.userid, msgType);
        }
    }

    @Override protected String getChannelName() { return "WEB"; }
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

                if("S".equalsIgnoreCase(msgType)){
                    // SMS 별도 처리 없음
                } else if("L".equalsIgnoreCase(msgType)){
                    // LMS 별도 처리 없음
                } else if ( "M".equalsIgnoreCase(msgType)) {
                    boolean hasImage = (bean.getFilepath1() != null && !bean.getFilepath1().trim().isEmpty()) ||
                            (bean.getFilepath2() != null && !bean.getFilepath2().trim().isEmpty()) ||
                            (bean.getFilepath3() != null && !bean.getFilepath3().trim().isEmpty());

                    if (hasImage) {
                        String imageId = uploadMmsImages(bean);

                        if(imageId != null) {
                            bean.setMmsimageid(imageId);
                        } else {
                            bean.setSmskind("L");
                        }
                    }else{
                        bean.setSmskind("L");
                    }
                } else if ("T".equalsIgnoreCase(msgType)) {

                    if (bean.getMessagetype() == null || bean.getMessagetype().trim().isEmpty()) {
                        bean.setMessagetype("AT");
                    }

                    if("AT".equalsIgnoreCase(bean.getMessagetype())){
                        parseRmsButton(bean, mapper);
                    }else if ("FT".equalsIgnoreCase(bean.getMessagetype())){
                        bean.setMessagetype("E1");
                        parseBrandMessageButton(bean, mapper);
                    }

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
                }

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

                log.error("[WEB - {}] 데이터 정제 실패! 발송 제외 처리됨. ({}건)", msgType, invalidList.size());
            }
        } catch (Exception e) {
            log.error("[WEB-알림톡] 데이터 조회/정제 오류: {}", e.getMessage());
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
            log.error("[WEB-알림톡] 상태값 업데이트 오류: {}", e.getMessage());
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

    private void parseBrandMessageButton(RequestBean bean, ObjectMapper mapper) {
        String rawButton = bean.getButton();

        if (rawButton == null || rawButton.trim().isEmpty()) {
            return;
        }

        try {
            ObjectNode attachmentNode = mapper.createObjectNode();
            ArrayNode buttonArray = mapper.createArrayNode();

            // 버튼 여러 개 분리
            String[] btnArray = rawButton.split("\\|");

            // 최대 5개 처리
            for (int i = 0; i < btnArray.length && i < 5; i++) {

                // 버튼 정보 분리
                String[] btnInfo = btnArray[i].split("\\^", -1);

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

                    buttonArray.add(btnNode);
                }
            }

            // attachments 구조
            attachmentNode.set("button", buttonArray);

            bean.setAttachments(mapper.writeValueAsString(attachmentNode));

        } catch (Exception e) {
            log.error("[RMS] 브랜드메시지 버튼 생성 실패 (msgid: {}): {}",
                    bean.getMsgid(), e.getMessage());
        }
    }

    private String uploadMmsImages(RequestBean bean) {
        String[] paths = {bean.getFilepath1(), bean.getFilepath2(), bean.getFilepath3()};
        String[] keys = {"image1", "image2", "image3"};

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("userid", this.userid); // Go 서버가 요구하는 userid 폼 데이터

        boolean hasFile = false;

        // 1. 첨부파일 1~3번을 확인하고, 실제 파일이 존재하면 바디에 장전!
        for (int i = 0; i < 3; i++) {
            if (paths[i] != null && !paths[i].trim().isEmpty()) {
                File file = new File(paths[i]);
                if (file.exists() && file.isFile()) {
                    body.add(keys[i], new FileSystemResource(file));
                    hasFile = true;
                } else {
                    log.info("[RMS] MMS DB 경로는 있으나 실제 파일이 없음 (무시됨): {}", paths[i]);
                }
            }
        }

        // 2. 보낼 파일이 아예 없다면 쿨하게 null 리턴
        if (!hasFile) {
            return null;
        }

        try {
            // 3. Multipart 통신 헤더 세팅
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();

            // ⭐️ 형님의 Go 서버 MMS 업로드 API 주소 (환경에 맞게 수정해주세요!)
            String uploadUrl = this.dhnServer + "mms/image";

            // 4. API 발사!
            ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, requestEntity, String.class);

            // 5. 성공(200) 시 JSON 까서 image_group 리턴!
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                if (root.has("image_group")) {
                    return root.get("image_group").asText();
                }
            } else {
                log.error("[RMS] MMS 이미지 업로드 API 에러 응답: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("[RMS] MMS 이미지 업로드 통신 장애: {}", e.getMessage());
        }
        return null; // 실패 시 null
    }
}