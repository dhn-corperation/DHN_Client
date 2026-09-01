package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ErpSendAgent extends AbstractSendAgent {

    @Autowired
    @Qualifier("erpService")
    private RequestService requestService;

    @Value("${dhnclient.erp.userid:}")
    private String userid;

    @Value("${dhnclient.server:}")
    private String dhnServer;

    @Value("${dhnclient.erp.db-target:mssql}")
    private String dbTarget;

    @Value("${dhnclient.erp.msg_table:MTMSG_DATA}")
    private String msgTable;

    @Value("${dhnclient.erp.log_table:MTMSG_LOG}")
    private String logTable;

    @Value("${dhnclient.erp_use:N}")
    private String erpUse;

    @Value("${dhnclient.erp.mms_path:}")
    private String mmsPath;

    @Scheduled(fixedDelay = 1000)
    public void SendProcess() {
        if (!"Y".equalsIgnoreCase(erpUse)) {
            return;
        }
        String[] msgTypes = {"AT", "FT", "LMS", "SMS", "MMS", "BM"};
        for (String msgType : msgTypes) {
            super.executeProcess(this.dhnServer, this.userid, msgType);
        }
    }

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
            param.setDatabase(dbTarget);

            List<RequestBean> rawList = requestService.selectRequests(param);

            if (rawList == null || rawList.isEmpty()) {
                return finalSendList;
            }

            ObjectMapper mapper = JsonMapper.builder()
                    .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                    .build();

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

            requestService.updateSendComplete(param);
        } catch (Exception e) {
            log.error("[ERP] 상태값 업데이트 오류: {}", e.getMessage());
        }
    }

    private void parseCustomButtonJson(RequestBean bean, ObjectMapper mapper) {
        String rawButton = bean.getButton();
        if (rawButton == null || rawButton.trim().isEmpty()) {
            return;
        }

        try {
            JsonNode root = mapper.readTree(rawButton);

            if (root.has("pass") && root.get("pass").has("extra") && root.get("pass").get("extra").has("msg_type")) {
                String extraMsgType = root.get("pass").get("extra").get("msg_type").asText();
                if (extraMsgType != null && !extraMsgType.isEmpty()) {
                    bean.setMessagetype(extraMsgType);
                }
            }

            JsonNode imageNode = null;
            if (root.has("pass") && root.get("pass").has("image")) {
                imageNode = root.get("pass").get("image");
            } else if (root.has("image")) {
                imageNode = root.get("image");
            }

            if (imageNode != null) {
                if (imageNode.has("img_url")) {
                    bean.setImageurl(imageNode.get("img_url").asText());
                }
                if (imageNode.has("img_link")) {
                    bean.setImagelink(imageNode.get("img_link").asText());
                }
            }
            JsonNode btnArray = null;
            if (root.has("pass") && root.get("pass").has("button")) {
                btnArray = root.get("pass").get("button");
            } else if (root.has("button")) {
                btnArray = root.get("button");
            } else if (root.isArray()) {
                btnArray = root;
            }

            bean.setButton1(null);
            bean.setButton2(null);
            bean.setButton3(null);
            bean.setButton4(null);
            bean.setButton5(null);

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
            log.warn("[ERP] 버튼/이미지 JSON 변환 스킵 또는 예외 (msgid: {}): {}", bean.getMsgid(), e.getMessage());
        }
    }

    private void parseBrandMessageJson(RequestBean bean, ObjectMapper mapper) {
        String rawMsg = bean.getMsg();

        if (rawMsg == null || rawMsg.trim().isEmpty()) {
            return;
        }
        if (rawMsg == null || !rawMsg.trim().startsWith("{")) {
            return;
        }

        try {
            JsonNode root = mapper.readTree(rawMsg);

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
                    bean.setSmskind("L");
                }

            }else{
                bean.setMsg("");
                bean.setMsgsms("");
            }

            if (root.has("targeting")) {
                if(root.get("targeting").asText().equalsIgnoreCase("I")){
                    bean.setKind("O");
                }else{
                    bean.setKind(root.get("targeting").asText());
                }
            }

            if (root.has("header")) {
                bean.setHeader(root.get("header").asText());
            }

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

            if (root.has("msgType")) {
                String innerMsgType = root.get("msgType").asText();
                String mappedBType = mapBrandMessageType(innerMsgType);
                bean.setMessagetype(mappedBType);
            }

        } catch (Exception e) {
            log.error("[ERP] 브랜드톡 JSON 해체 실패 (msgid: {}): {}", bean.getMsgid(), e.getMessage());
        }
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

    private String uploadMmsImages(RequestBean bean) {
        String[] dbPaths = {
                bean.getFilepath1(),
                bean.getFilepath2(),
                bean.getFilepath3()
        };
        String[] keys = {"image1", "image2", "image3"};

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("userid", this.userid);

        boolean hasFile = false;

        for (int i = 0; i < 3; i++) {
            String path = getMmsFilePath(dbPaths[i]);

            if (path != null && !path.trim().isEmpty()) {
                File file = new File(path);

                if (file.exists() && file.isFile()) {
                    body.add(keys[i], new FileSystemResource(file));
                    hasFile = true;
                } else {
                    log.info("[ERP] MMS 이미지 파일 없음: {}", path);
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

            String uploadUrl = this.dhnServer + "mms/image";

            ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                if (root.has("image_group")) {
                    return root.get("image_group").asText();
                }
            } else {
                log.error("[ERP] MMS 이미지 업로드 API 에러 응답: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("[ERP] MMS 이미지 업로드 통신 장애: {}", e.getMessage());
        }
        return null;
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
}