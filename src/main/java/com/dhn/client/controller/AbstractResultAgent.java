package com.dhn.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public abstract class AbstractResultAgent {

    protected boolean isProc = false; // dual 관련 로직이 빠지면서 isStart도 제거했습니다!
    protected static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // 자식 클래스가 반드시 구현해야 하는 필수 설정들 (modId, dual 싹 제거됨!)
    protected abstract String getChannelName();
    protected abstract String getDbTarget();
    protected abstract String getDhnServer();
    protected abstract String getUserid();
    protected abstract String getMsgTable();
    protected abstract String getLogTable();

    // 결과 처리 로직 자체를 자식이 알아서 구현하도록 강제함
    protected abstract void resultProc(JSONArray json);

    protected void initAgent() {
        log.info("[{}] Result Agent 초기화 완료 (Userid: {})", getChannelName(), getUserid());
    }

    // 공통 결과 수신 네트워크 엔진
    protected void executeResultProcess() {
        if (isProc) return;

        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) executorService;
        if (poolExecutor.getActiveCount() >= poolExecutor.getMaximumPoolSize()) return;

        isProc = true;
        try {
            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.APPLICATION_JSON);
            header.set("userid", getUserid());

            RestTemplate rt = new RestTemplate();
            HttpEntity<String> entity = new HttpEntity<>(null, header);

            ResponseEntity<String> response = rt.postForEntity(getDhnServer() + "result", entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                JSONObject jsonObject = new JSONObject(responseBody);

                if (jsonObject.has("data")) {
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    if (dataObject.has("detail")) {
                        JSONArray jsonArray = dataObject.getJSONArray("detail");
                        if (jsonArray.length() > 0) {
                            executorService.submit(() -> resultProc(jsonArray));
                        }
                    } else {
                        log.error("[{}] 결과 수신 오류 : 결과 배열(detail)이 없습니다.", getChannelName());
                    }
                } else {
                    log.error("[{}] 결과 수신 오류 : (data) 필드가 없습니다.", getChannelName());
                }
            } else {
                log.info("[{}] 결과 수신 오류 (Http Err) : {}", getChannelName(), response.getStatusCode());
            }
        } catch (Exception ex) {
            log.error("[{}] 결과 수신 오류: {}", getChannelName(), ex.getMessage());
        } finally {
            isProc = false;
        }
    }
}