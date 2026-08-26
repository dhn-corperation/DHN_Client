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

    protected boolean isProc = false;
    protected static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    protected static volatile boolean isRunning = true;

    protected abstract String getChannelName();
    protected abstract String getDbTarget();
    protected abstract String getDhnServer();
    protected abstract String getUserid();

    protected abstract void resultProc(JSONArray json);

    protected void initAgent() {
        log.info("[{}] Result Agent 초기화 완료 (Userid: {})", getChannelName(), getUserid());
    }

    public static void onShutDown() {
        log.warn("서버 종료 요청 결과처리 프로세스 종료");
        isRunning = false;
    }

    protected void executeResultProcess() {
        if (isProc) return;

        if (!isRunning) {
            return;
        }

        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) executorService;
        if (poolExecutor.getActiveCount() >= poolExecutor.getMaximumPoolSize()) return;

        isProc = true;
        try {
            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.APPLICATION_JSON);
            header.set("userid", getUserid());

            org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                    new org.springframework.http.client.SimpleClientHttpRequestFactory();

            factory.setConnectTimeout(5000); // 연결 타임아웃: 5초 (서버와 커넥션 맺을 때 최대 대기 시간)
            factory.setReadTimeout(10000);   // 읽기 타임아웃: 10초 (서버가 응답 데이터를 줄 때 최대 대기 시간)

            RestTemplate rt = new RestTemplate(factory);
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