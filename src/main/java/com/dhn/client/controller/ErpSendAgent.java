package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
public class ErpSendAgent extends AbstractSendAgent {

    @Autowired
    @Qualifier("erpService") // ⭐️ ERP 서비스 주입!
    private RequestService requestService;

    @Value("${dhnclient.erp.userid:}")
    private String userid;

    @Value("${dhnclient.server:}")
    private String dhnServer;

    // ⭐️ 단일 DB로 가더라도 부모 규격을 위해 변수는 유지!
    @Value("${dhnclient.erp.db-target:oracle}")
    private String dbTarget;

    @Value("${dhnclient.erp.msg_table:MTMSG_DATA}")
    private String msgTable;

    @Value("${dhnclient.erp.log_table:MTMSG_LOG}")
    private String logTable;

    @Value("${dhnclient.erp_use:N}")
    private String erpUse;

    // ⏰ 1초마다 돌면서 msgType 별로 부모의 스레드풀에 작업을 던집니다!
    @Scheduled(fixedDelay = 1000)
    public void SendProcess() {
        if (!"Y".equalsIgnoreCase(erpUse)) {
            return;
        }
        // ERP에서 사용할 발송 타입들 (필요에 따라 수정)
        String[] msgTypes = {"AT", "FT", "LMS", "SMS", "BM"};
        for (String msgType : msgTypes) {
            // ⭐️ 자물쇠를 풀고 묶는 부모의 강력한 병렬 프로세스를 호출
            super.executeProcess(this.dhnServer, this.userid, msgType);
        }
    }

    // ==========================================
    // ⭐️ 부모의 추상 메서드 구현 영역 (규격 100% 일치)
    // ==========================================

    @Override
    protected String getChannelName() { return "ERP"; }

    @Override
    protected String getDbTarget() { return this.dbTarget; }

    @Override
    protected List<RequestBean> fetchWaitingData(String msgType) {
        List<RequestBean> finalSendList = new ArrayList<>();
        List<String> invalidList = new ArrayList<>();

        try {
            SQLParameter param = new SQLParameter();
            param.setMsg_table(msgTable);
            param.setMsg_type(msgType);
            param.setDatabase(dbTarget); // SQL 빈값 방지

            List<RequestBean> rawList = requestService.selectRequests(param);

            if (rawList == null || rawList.isEmpty()) {
                return finalSendList;
            }

            ObjectMapper mapper = JsonMapper.builder()
                    .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                    .build();

            for (RequestBean bean : rawList) {
                // 1. 번호 유효성 검사
                if (bean.getPhn() == null || bean.getPhn().length() < 10) {
                    invalidList.add(bean.getMsgid());
                    continue;
                }


                if ("SMS".equalsIgnoreCase(msgType)) {
                    bean.setSmskind("S");
                } else if("LMS".equalsIgnoreCase(msgType)){
                    bean.setSmskind("L");
                } else if("AT".equalsIgnoreCase(msgType)){

                    parseCustomButtonJson(bean, mapper);

                    try {
                        byte[] msgBytes = bean.getMsg() != null ? bean.getMsg().getBytes("EUC-KR") : new byte[0];
                        bean.setSmskind(msgBytes.length > 90 ? "L" : "S");
                    } catch (Exception e) {
                        bean.setSmskind("S");
                    }
                } else if ("FT".equalsIgnoreCase(msgType)) {

                } else if ("BM".equalsIgnoreCase(msgType)) {
                    parseBrandMessageJson(bean, mapper);
                }

                // 3. JSON 페이로드 정제
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
                ml.setResult_message("(AGENT) 데이터 형식 또는 정제 오류");
                ml.setCode("7999");
                ml.setDatabase(dbTarget);

                requestService.updateInvalidData(invalidList, ml);
                log.error("[ERP - {}] 데이터 정제 실패! 발송 제외 처리됨. ({}건)", msgType, invalidList.size());
            }

        } catch (Exception e) {
            log.error("[ERP - {}] 데이터 조회/정제 오류: {}", msgType, e.getMessage());
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

            // 부모/서비스 규격에 맞춰 updateSendComplete 호출
            requestService.updateSendComplete(param);
        } catch (Exception e) {
            log.error("[ERP] 상태값 업데이트 오류: {}", e.getMessage());
        }
    }

    /**
     * ⭐️ 버튼 및 이미지 통 JSON 해체 전용 메서드
     * 예: {"pass":{"extra":{"msg_type":"FI"}, "button":[...], "image":{"img_url":"...", "img_link":"..."}}}
     */
    private void parseCustomButtonJson(RequestBean bean, ObjectMapper mapper) {
        String rawButton = bean.getButton1(); // DB에서 BUTTON 컬럼을 임시로 button1에 담아왔다고 가정
        if (rawButton == null || rawButton.trim().isEmpty()) {
            return;
        }

        try {
            JsonNode root = mapper.readTree(rawButton);

            // 1. [타입 자동 보정] pass -> extra -> msg_type
            if (root.has("pass") && root.get("pass").has("extra") && root.get("pass").get("extra").has("msg_type")) {
                String extraMsgType = root.get("pass").get("extra").get("msg_type").asText();
                if (extraMsgType != null && !extraMsgType.isEmpty()) {
                    bean.setMessagetype(extraMsgType);
                }
            }

            // =========================================================================
            // 2. ⭐️ [친구톡 이미지 추출] pass -> image 노드 해체
            // =========================================================================
            JsonNode imageNode = null;
            if (root.has("pass") && root.get("pass").has("image")) {
                imageNode = root.get("pass").get("image");
            } else if (root.has("image")) {
                imageNode = root.get("image");
            }

            if (imageNode != null) {
                // img_url -> imageurl 에 세팅
                if (imageNode.has("img_url")) {
                    bean.setImageurl(imageNode.get("img_url").asText());
                }
                // img_link -> imagelink 에 세팅 (JSON에 있을 경우 대비)
                if (imageNode.has("img_link")) {
                    bean.setImagelink(imageNode.get("img_link").asText());
                }
            }
            // =========================================================================

            // 3. [버튼 해체] pass -> button 배열 추출
            JsonNode btnArray = null;
            if (root.has("pass") && root.get("pass").has("button")) {
                btnArray = root.get("pass").get("button");
            } else if (root.has("button")) {
                btnArray = root.get("button");
            } else if (root.isArray()) {
                btnArray = root;
            }

            // 원본 통 문자열이 들어있던 button1 을 일단 초기화
            bean.setButton1(null);
            bean.setButton2(null);
            bean.setButton3(null);
            bean.setButton4(null);
            bean.setButton5(null);

            // 4. 배열 순서대로 button1 ~ button5 에 개별 JSON 문자열로 세팅
            if (btnArray != null && btnArray.isArray()) {
                for (int i = 0; i < btnArray.size(); i++) {
                    String singleBtnJson = mapper.writeValueAsString(btnArray.get(i));
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
            // 통 JSON 양식이 아니거나 일반 텍스트인 경우 기존 세팅 유지
            log.warn("[ERP] 버튼/이미지 JSON 변환 스킵 또는 예외 (msgid: {}): {}", bean.getMsgid(), e.getMessage());
        }
    }

    /**
     * ⭐️ [신규] 브랜드톡 통 JSON 해체 전용 메서드
     * 입력(MESSAGE): {"sendType":"free","msgType":"FT","targeting":"I","text":"...","header":"...","attachment":{...}}
     */
    private void parseBrandMessageJson(RequestBean bean, ObjectMapper mapper) {
        String rawMsg = bean.getMsg(); // MESSAGE 컬럼 값이 통째로 msg에 들어있음

        if (rawMsg == null || rawMsg.trim().isEmpty()) {
            return;
        }
        // JSON 형태가 아니면 스킵
        if (rawMsg == null || !rawMsg.trim().startsWith("{")) {
            return;
        }

        try {
            JsonNode root = mapper.readTree(rawMsg);

            // 1. 텍스트 추출 -> RequestBean의 msg 로 세팅
            if (root.has("text")) {
                String innerMsgType = root.get("msgType").asText();
                bean.setMsg(root.get("text").asText());

            }else{
                bean.setMsg("");
                bean.setMsgsms("");
            }

            // 2. targeting 추출 -> RequestBean의 kind 로 세팅
            if (root.has("targeting")) {
                if(root.get("targeting").asText().equalsIgnoreCase("I")){
                    bean.setKind("O");
                }else{
                    bean.setKind(root.get("targeting").asText());
                }
            }

            // 3. header 추출 -> RequestBean의 title (또는 header) 로 세팅
            if (root.has("header")) {
                bean.setHeader(root.get("header").asText()); // 보통 DHN 규격상 title 필드로 맵핑됨
            }

            // 4. attachment 통 덩어리 추출 -> RequestBean의 attachment 필드에 JSON 문자열로 그대로 세팅!
            if (root.has("attachment")) {
                String attachmentStr = mapper.writeValueAsString(root.get("attachment"));
                attachmentStr = convertCamelToSnakeForUrls(attachmentStr);
                bean.setAttachments(attachmentStr);
            }

            if (root.has("carousel")) {
                String carouselStr = mapper.writeValueAsString(root.get("carousel"));
                carouselStr = convertCamelToSnakeForUrls(carouselStr);
                bean.setCarousel(carouselStr);
            }

            if (root.has("adFlag")) {
                bean.setAdflag(root.get("adFlag").asText());
            }

            // 5. msgType 추출 -> API 규격인 B1 ~ B8 로 변환하여 messagetype 세팅!
            if (root.has("msgType")) {
                String innerMsgType = root.get("msgType").asText();
                String mappedBType = mapBrandMessageType(innerMsgType);
                bean.setMessagetype(mappedBType);
            }

        } catch (Exception e) {
            log.error("[ERP] 브랜드톡 JSON 해체 실패 (msgid: {}): {}", bean.getMsgid(), e.getMessage());
        }
    }

    /**
     * ⭐️ 브랜드톡 msgType (FT, FI 등)을 API 규격(B1 ~ B8)으로 변환해주는 매퍼
     */
    private String mapBrandMessageType(String msgType) {
        if (msgType == null) return "E1";

        switch (msgType.toUpperCase()) {
            case "FT": return "E1"; // 브랜드톡 기본 텍스트
            case "FI": return "E2"; // 브랜드톡 이미지
            case "FW": return "E3"; // 브랜드톡 와이드 이미지
            case "FL": return "E4"; // 브랜드톡 와이드 리스트
            case "FC": return "E5"; // 브랜드톡 캐러셀 피드
            case "FP": return "E6"; // 브랜드톡 프리미엄 동영상
            case "FM": return "E7"; // 브랜드톡 커머스
            case "FA": return "E8"; // 브랜드톡 캐러셀 커머스
            default:   return "E1";
        }
    }

    private String convertCamelToSnakeForUrls(String jsonStr) {
        if (jsonStr == null) return null;

        return jsonStr.replace("\"urlMobile\":", "\"url_mobile\":")
                .replace("\"urlPc\":", "\"url_pc\":")
                .replace("\"imgUrl\":", "\"img_url\":")
                .replace("\"imgLink\":", "\"img_link\":")
                .replace("\"schemeIos\":", "\"scheme_ios\":")
                .replace("\"schemeAndroid\":", "\"scheme_android\":")
                .replace("\"regularPrice\":", "\"regular_price\":")
                .replace("\"discountPrice\":", "\"discount_price\":")
                .replace("\"discountRate\":", "\"discount_rate\":")
                .replace("\"discountFixed\":", "\"discount_fixed\":")
                .replace("\"videoUrl\":", "\"video_url\":")
                .replace("\"thumbnailUrl\":", "\"thumbnail_url\":")
                .replace("\"additionalContent\":", "\"additional_content\":")
                .replace("\"imageUrl\":", "\"image_url\":");
    }
}