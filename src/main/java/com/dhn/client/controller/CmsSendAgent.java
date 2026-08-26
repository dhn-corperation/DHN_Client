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
import java.nio.file.Paths;
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
            param.setDatabase(dbTarget);

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

                    parseButton(bean, mapper);

                    try {
                        byte[] msgBytes = bean.getMsg() != null ? bean.getMsg().getBytes("EUC-KR") : new byte[0];
                        bean.setSmskind(msgBytes.length > 90 ? "L" : "S");
                    } catch (Exception e) {
                        bean.setSmskind("L");
                    }
                } else if ("BM".equalsIgnoreCase(msgType)) {
                    bean.setMessagetype("E1");
                    parseBrandButtonJson(bean, mapper);
                    try {
                        byte[] msgBytes = bean.getMsg() != null ? bean.getMsg().getBytes("EUC-KR") : new byte[0];
                        bean.setSmskind(msgBytes.length > 90 ? "L" : "S");
                    } catch (Exception e) {
                        bean.setSmskind("L");
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

    private void parseButton(RequestBean bean, ObjectMapper mapper) {
        String rawButton = bean.getButton();

        if (rawButton == null || rawButton.trim().isEmpty()) {
            return;
        }

        try {
            JsonNode root = mapper.readTree(rawButton);

            if (!root.isArray()) {
                log.error("[CMS] BUTTON JSON 배열 형식 오류 (msgid: {})", bean.getMsgid());
                return;
            }

            ArrayNode btnArray = (ArrayNode) root;

            for (int i = 0; i < btnArray.size() && i < 5; i++) {
                String buttonJson = mapper.writeValueAsString(btnArray.get(i));

                switch (i) {
                    case 0:
                        bean.setButton1(buttonJson);
                        break;
                    case 1:
                        bean.setButton2(buttonJson);
                        break;
                    case 2:
                        bean.setButton3(buttonJson);
                        break;
                    case 3:
                        bean.setButton4(buttonJson);
                        break;
                    case 4:
                        bean.setButton5(buttonJson);
                        break;
                }
            }

            if (btnArray.size() > 5) {
                log.warn("[CMS] BUTTON 최대 5개 초과 (msgid: {}, count: {})",bean.getMsgid(), btnArray.size());
            }

        } catch (Exception e) {
            log.error("[CMS] 버튼 JSON 파싱 실패 (msgid: {}): {}",bean.getMsgid(), e.getMessage());
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
                    log.info("[CMS] MMS 이미지 파일 없음: {}", path);
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
                log.error("[CMS] MMS 이미지 업로드 API 에러 응답: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("[CMS] MMS 이미지 업로드 통신 장애: {}", e.getMessage());
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

    private void parseBrandButtonJson(RequestBean bean, ObjectMapper mapper) {
        String rawButton = bean.getButton();

        if (rawButton == null || rawButton.trim().isEmpty()) {
            return;
        }

        try {
            JsonNode buttonNode = mapper.readTree(rawButton);

            if (!buttonNode.isArray()) {
                log.error("[CMS] 브랜드메시지 BUTTON JSON 배열 형식 오류 (msgid: {})", bean.getMsgid());
                return;
            }

            ObjectNode attachmentNode = mapper.createObjectNode();
            attachmentNode.set("button", buttonNode);

            bean.setAttachments(mapper.writeValueAsString(attachmentNode));

        } catch (Exception e) {
            log.error("[CMS] 브랜드메시지 버튼 JSON 파싱 실패 (msgid: {}): {}", bean.getMsgid(), e.getMessage());
        }
    }
}