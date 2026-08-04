package com.dhn.client.controller;

import com.dhn.client.bean.RequestBean;
import com.dhn.client.config.DbContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractSendAgent {

    protected static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    protected static volatile boolean isRunning = true;

    // ⭐️ 5개 채널(msgType)이 서로 막히지 않게 개별 자물쇠(Map) 생성!
    protected final Map<String, Boolean> procMap = new ConcurrentHashMap<>();

    // 자식이 구현해야 할 추상 메서드들
    protected abstract String getChannelName();
    protected abstract String getDbTarget();
    protected abstract List<RequestBean> fetchWaitingData(String msgType);
    protected abstract void updateStatusToSent(List<String> msgIds);

    public static void onShutDown() {
        log.warn("서버 종료 요청 발송 프로세스 종료");
        isRunning = false;
    }

    public void executeProcess(String dhnServer, String userid, String msgType) {
        if (!isRunning) {
            return;
        }

        // 내 채널(msgType)이 처리 중이면 패스
        if (procMap.getOrDefault(msgType, false)) return;
        procMap.put(msgType, true); // 내 자물쇠 잠금

        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) executorService;
        if (poolExecutor.getActiveCount() >= poolExecutor.getMaximumPoolSize()) {
            procMap.put(msgType, false);
            return;
        }

//        final String targetDb = getDbTarget();
//
//        executorService.submit(() -> {
//            DbContextHolder.setDbTarget(targetDb);
//            try {
//                // 1. 대기 상태 데이터 셀렉 & 정제 (자식이 구현)
//                List<RequestBean> sendList = fetchWaitingData(msgType);
//
//                if (sendList == null || sendList.isEmpty()) {
//                    return;
//                }
//
//                // 2. API 전송 후 상태값 변경
//                sendToApiAndUpdateStatus(sendList, dhnServer, userid, msgType);
//
//            } catch (Exception e) {
//                log.error("[{}-{}] 발송 프로세스 오류: {}", getChannelName(), msgType, e.getMessage());
//            } finally {
//                DbContextHolder.clear();
//                // ⭐️ 로직이 끝나면 반드시 내 채널의 자물쇠를 풀어준다!
//                procMap.put(msgType, false);
//            }
//        });

        executorService.submit(() -> {
            try {
                // 1. 대기 상태 데이터 셀렉 & 정제 (자식이 구현)
                List<RequestBean> sendList = fetchWaitingData(msgType);

                if (sendList == null || sendList.isEmpty()) {
                    return;
                }

                // 2. API 전송 후 상태값 변경
                sendToApiAndUpdateStatus(sendList, dhnServer, userid, msgType);

            } catch (Exception e) {
                log.error("[{}-{}] 발송 프로세스 오류: {}", getChannelName(), msgType, e.getMessage());
            } finally {
                procMap.put(msgType, false);
            }
        });
    }

    private void sendToApiAndUpdateStatus(List<RequestBean> sendList, String dhnServer, String userid, String msgType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, sendList);

            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.APPLICATION_JSON);
            header.set("userid", userid);
            header.set("v2flag", "1");

            HttpEntity<String> entity = new HttpEntity<>(sw.toString(), header);

            org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                    new org.springframework.http.client.SimpleClientHttpRequestFactory();

            factory.setConnectTimeout(5000); // 연결 타임아웃: 5초 (서버와 커넥션 맺을 때 최대 대기 시간)
            factory.setReadTimeout(10000);   // 읽기 타임아웃: 10초 (서버가 응답 데이터를 줄 때 최대 대기 시간)

            RestTemplate rt = new RestTemplate(factory);

            // ⭐️ "req" -> "send" 로 API 엔드포인트 통일!
            ResponseEntity<String> response = rt.postForEntity(dhnServer + "req", entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("[{}-{}] 외부 API 전송 완료 ({} 건)", getChannelName(), msgType, sendList.size());

                List<String> msgIds = sendList.stream()
                        .map(RequestBean::getMsgid)
                        .collect(Collectors.toList());

                // 3. 자식의 업데이트 메서드 호출
                updateStatusToSent(msgIds);
            } else {
                log.error("[{}-{}] API 전송 에러 응답: {}", getChannelName(), msgType, response.getBody());
            }
        } catch (Exception e) {
            log.error("[{}-{}] API 통신 장애: {}", getChannelName(), msgType, e.getMessage());
        }
    }
}