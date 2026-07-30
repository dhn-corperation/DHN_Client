package com.dhn.client.controller;

import com.dhn.client.bean.RequestBean;
import com.dhn.client.config.DbContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

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

    // ⭐️ 핵심: 5개 채널이 서로 막히지 않게 개별 자물쇠(Map) 생성!
    protected final Map<String, Boolean> procMap = new ConcurrentHashMap<>();

    // 자식이 구현해야 할 추상 메서드들
    protected abstract String getChannelName();
    protected abstract String getDbTarget();
    protected abstract List<RequestBean> fetchWaitingData(String msgType);
    protected abstract void updateStatusToSent(List<String> msgIds);

    public void executeProcess(String dhnServer, String userid, String msgType) {
        // ⭐️ 내 채널(msgType)이 처리 중이면 패스 (다른 채널은 안 막힘!)
        if (procMap.getOrDefault(msgType, false)) return;
        procMap.put(msgType, true); // 내 자물쇠 잠금

        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) executorService;
        if (poolExecutor.getActiveCount() >= poolExecutor.getMaximumPoolSize()) {
            procMap.put(msgType, false); // 자물쇠 풀고 리턴
            return;
        }

        final String targetDb = getDbTarget();

        executorService.submit(() -> {
            DbContextHolder.setDbTarget(targetDb);
            try {
                // 1. 발송타입 and 발송대기 상태 데이터를 셀렉 & 정제
                List<RequestBean> sendList = fetchWaitingData(msgType);

                // 1-1. row가 0이면 해당 프로세스를 종료한다.
                if (sendList == null || sendList.isEmpty()) {
                    return;
                }

                // 2 & 3. API 전송 후 상태값 변경
                sendToApiAndUpdateStatus(sendList, dhnServer, userid);

            } catch (Exception e) {
                log.error("[{}] 발송 프로세스 오류: {}", getChannelName(), e.getMessage());
            } finally {
                DbContextHolder.clear();
                // ⭐️ 로직이 끝나면 반드시 내 채널의 자물쇠를 풀어준다!
                procMap.put(msgType, false);
            }
        });
    }

    private void sendToApiAndUpdateStatus(List<RequestBean> sendList, String dhnServer, String userid) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, sendList);

            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.APPLICATION_JSON);
            header.set("userid", userid);

            HttpEntity<String> entity = new HttpEntity<>(sw.toString(), header);
            RestTemplate rt = new RestTemplate();

            // 3. 목적지에 데이터 전송
            ResponseEntity<String> response = rt.postForEntity(dhnServer + "req", entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("[{}] 외부 API 전송 완료 ({} 건)", getChannelName(), sendList.size());

                // 성공한 ID 목록 추출
                List<String> msgIds = sendList.stream()
                        .map(RequestBean::getMsgid)
                        .collect(Collectors.toList());

                // 3. SMS_STATUS 필드를 1로 변경 호출
                updateStatusToSent(msgIds);
            } else {
                log.error("[{}] API 전송 에러 응답: {}", getChannelName(), response.getBody());
            }
        } catch (Exception e) {
            log.error("[{}] API 통신 장애: {}", getChannelName(), e.getMessage());
        }
    }
}