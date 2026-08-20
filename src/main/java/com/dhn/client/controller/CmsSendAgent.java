package com.dhn.client.controller;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class CmsSendAgent extends AbstractSendAgent { // ⭐️ 범인 2: 부모 상속 누락 해결!

    @Autowired
    @Qualifier("cmsService")
    private RequestService requestService;

    // ⭐️ 범인 1 해결: yml 명세와 완벽하게 일치하는 변수명과 안전한 기본값!
    @Value("${dhnclient.cms.userid:}")
    private String userid;

    @Value("${dhnclient.server:}")
    private String dhnServer;

    @Value("${dhnclient.cms.db-target:mssql}")
    private String dbTarget;

    @Value("${dhnclient.cms.msg_table:TBL_SUBMIT_QUEUE}")
    private String msgTable;

    @Value("${dhnclient.cms.log_table:TBL_MSG_HIST}")
    private String logTable;

    @Value("${dhnclient.cms_use:N}")
    private String cmsUse;

    // ⏰ 0.1초마다 돌면서 msgType 별로 부모의 스레드풀에 작업을 던집니다!
    @Scheduled(fixedDelay = 1000)
    public void SendProcess() {
        if (!"Y".equalsIgnoreCase(cmsUse)) {
            return;
        }
        String[] msgTypes = {"SM", "LM"};
        for (String msgType : msgTypes) {
            // ⭐️ 자물쇠를 풀고 묶는 부모의 강력한 병렬 프로세스를 호출
            super.executeProcess(this.dhnServer, this.userid, msgType);
        }
    }

    // ==========================================
    // ⭐️ 부모의 추상 메서드 구현 영역
    // ==========================================

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

                // JSON 페이로드 정제
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

    private boolean isRealTimeData(RequestBean bean) {
        // TODO: 나중에 '특정 필드'가 정해지면 이 안의 조건을 수정해 주십쇼!
        // 예시 1: 예약 시간이 비어있으면 실시간으로 간주한다?
        // return bean.getReservedt() == null || bean.getReservedt().isEmpty();

        // 예시 2: 특정 템플릿 코드나 플래그 값이 'R(Realtime)' 이면 실시간?
        // return "R".equals(bean.getSomeRealTimeFlag());

        // (현재는 로직만 세팅해두고 false로 꺼둡니다)
        return false;
    }
}