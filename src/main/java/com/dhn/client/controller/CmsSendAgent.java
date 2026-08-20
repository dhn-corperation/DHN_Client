package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CmsSendAgent extends AbstractSendAgent {

    @Autowired
    @Qualifier("cmsService")
    private RequestService requestService;

    @Value("${dhnclient.cms.userid:}")
    private String userid;

    @Value("${dhnclient.server:}")
    private String dhnServer;

    @Value("${dhnclient.cms.db-target:mssql}")
    private String dbTarget;

    @Value("${dhnclient.cms.msg_table:MTMSG_DATA}")
    private String msgTable;

    @Value("${dhnclient.cms.log_table:MTMSG_LOG}")
    private String logTable;

    @Value("${dhnclient.cms_use:N}")
    private String cmsUse;

    @Value("${dhnclient.cms.mms_path:}")
    private String mmsPath;

    @Scheduled(fixedDelay = 1000)
    public void SendProcess() {
        if (!"Y".equalsIgnoreCase(cmsUse)) {
            return;
        }
        String[] msgTypes = {"SMS", "LMS", "MMS"};
        for (String msgType : msgTypes) {
            super.executeProcess(this.dhnServer, this.userid, msgType);
        }
    }

    @Override
    protected String getChannelName() { return "CMS"; }

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

            ObjectMapper mapper = new ObjectMapper();

            for (RequestBean bean : rawList) {
                if (bean.getPhn() == null || bean.getPhn().length() < 10) {
                    invalidList.add(bean.getMsgid());
                    continue;
                }

                if ("SMS".equalsIgnoreCase(msgType)) {
                    bean.setSmskind("S");
                } else if("LMS".equalsIgnoreCase(msgType)){
                    bean.setSmskind("L");
                } else if("MMS".equalsIgnoreCase(msgType)){
                    bean.setSmskind("M");
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
                } else if("AT".equalsIgnoreCase(msgType)){

                    parseCustomButtonJson(bean, mapper);

                    try {
                        byte[] msgBytes = bean.getMsg() != null ? bean.getMsg().getBytes("EUC-KR") : new byte[0];
                        bean.setSmskind(msgBytes.length > 90 ? "L" : "S");
                    } catch (Exception e) {
                        bean.setSmskind("S");
                    }
                } else if ("FT".equalsIgnoreCase(msgType)) {
                    convertFriendTalk(bean, mapper);
                } else if ("BM".equalsIgnoreCase(msgType)) {
                    parseBrandMessageJson(bean, mapper);
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
                ml.setStatus("6");
                ml.setResult_message("(AGENT) 데이터 형식 또는 정제 오류");
                ml.setCode("7999");
                ml.setDatabase(dbTarget);

                requestService.updateInvalidData(invalidList, ml);
                log.error("[CMS - {}] 데이터 정제 실패! 발송 제외 처리됨. ({}건)", msgType, invalidList.size());
            }

        } catch (Exception e) {
            log.error("[CMS - {}] 데이터 조회/정제 오류: {}", msgType, e.getMessage());
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
            log.error("[CMS] 상태값 업데이트 오류: {}", e.getMessage());
        }
    }

    private String uploadMmsImages(RequestBean bean) {
        String[] dbPaths = {
                bean.getFilepath1(),
                bean.getFilepath2(),
                bean.getFilepath3()
        };
        String[] keys = {"image1", "image2", "image3"};

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("userid", this.userid); // Go 서버가 요구하는 userid 폼 데이터

        boolean hasFile = false;

        for (int i = 0; i < 3; i++) {
            String path = getMmsFilePath(dbPaths[i]);

            if (path != null && !path.trim().isEmpty()) {
                File file = new File(path);

                if (file.exists() && file.isFile()) {
                    body.add(keys[i], new FileSystemResource(file));
                    hasFile = true;
                } else {
                    log.info("[RMS] MMS 이미지 파일 없음: {}", path);
                }
            }
        }

        if (!hasFile) {
            return null;
        }

        try {
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

    private String getMmsFilePath(String dbPath) {
        if (dbPath == null || dbPath.trim().isEmpty()) {
            return null;
        }

        if (mmsPath == null || mmsPath.trim().isEmpty()) {
            return dbPath;
        }

        String fileName = Paths.get(dbPath).getFileName().toString();

        return Paths.get(mmsPath, fileName).toString();
    }

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

    private void convertFriendTalk(RequestBean bean, ObjectMapper mapper) {

        try {
            byte[] msgBytes = bean.getMsg() != null
                    ? bean.getMsg().getBytes("EUC-KR")
                    : new byte[0];

            bean.setSmskind(msgBytes.length > 90 ? "L" : "S");
        } catch (Exception e) {
            bean.setSmskind("S");
        }

        String rawButton = bean.getButton();

        if (rawButton == null || rawButton.trim().isEmpty()) {
            return;
        }

        if (!rawButton.trim().startsWith("{")) {
            return;
        }

        try {

            JsonNode root = mapper.readTree(rawButton);
            JsonNode pass = root.path("pass");

            if (pass.has("extra")) {
                String msgType = pass.path("extra")
                        .path("msg_type")
                        .asText();

                bean.setMessagetype(mapBrandMessageType(msgType));
            }

            ObjectNode attachment = mapper.createObjectNode();

            if (pass.has("button")) {
                attachment.set("button", pass.get("button"));
            }

            if (pass.has("image")) {
                attachment.set("image", pass.get("image"));
            }

            if (attachment.size() > 0) {
                bean.setAttachments(
                        convertCamelToSnakeForUrls(
                                mapper.writeValueAsString(attachment)
                        )
                );
            }

        } catch (Exception e) {
            log.error("[ERP] 친구톡 JSON 변환 실패 (msgid: {}): {}",
                    bean.getMsgid(), e.getMessage());
        }
    }

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
                bean.setMsg(root.get("text").asText());
                bean.setMsgsms(root.get("text").asText());
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

    private void parseCustomButtonJson(RequestBean bean, ObjectMapper mapper) {
        String rawButton = bean.getButton(); // DB에서 BUTTON 컬럼을 임시로 button에 담아왔다고 가정
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
}