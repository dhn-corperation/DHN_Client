package com.dhn.client.controller; // ⭐️ 형님 패키지로 세팅 완료!

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

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CmsSendAgent extends AbstractSendAgent { // ⭐️ 1번 에러 해결!

    @Autowired
    @Qualifier("cmsService")
    private RequestService requestService; // ⭐️ RequestService 인터페이스 주입!

    @Value("${dhnclient.server}") private String dhnServer;
    @Value("${dhnclient.cms.userid}") private String userid;
    @Value("${dhnclient.cms.db-target}") private String dbTarget;
    @Value("${dhnclient.cms.msg_table}") private String msgTable;
    @Value("${dhnclient.cms.log_table}") private String logTable;

    // 알림톡, 브랜드톡 등 사용여부 플래그
    @Value("${dhnclient.cms.kakao_use}") private String kakaoUse;
    @Value("${dhnclient.cms.brand_use}") private String brandUse;
    @Value("${dhnclient.cms.sms_use}") private String smsUse;
    @Value("${dhnclient.cms.lms_use}") private String lmsUse;
    @Value("${dhnclient.cms.mms_use}") private String mmsUse;

    // ==========================================
    // ⏰ 스케줄러
    // ==========================================
    @Scheduled(fixedDelay = 1000)
    public void runAlimtalk() {
        if ("Y".equalsIgnoreCase(kakaoUse)) {
            // ⭐️ 2번 에러 해결! (dhnServer, userid, "AT" 순서로 딱 맞음)
            super.executeProcess(dhnServer, userid, "AT");
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void runBrandMsg() {
        if ("Y".equalsIgnoreCase(brandUse)) {
            super.executeProcess(dhnServer, userid, "BM");
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void runSms() {
        if ("Y".equalsIgnoreCase(smsUse)) {
            // ⭐️ 2번 에러 해결! (dhnServer, userid, "AT" 순서로 딱 맞음)
            super.executeProcess(dhnServer, userid, "SM");
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void runLms() {
        if ("Y".equalsIgnoreCase(lmsUse)) {
            // ⭐️ 2번 에러 해결! (dhnServer, userid, "AT" 순서로 딱 맞음)
            super.executeProcess(dhnServer, userid, "LM");
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void runMms() {
        if ("Y".equalsIgnoreCase(mmsUse)) {
            // ⭐️ 2번 에러 해결! (dhnServer, userid, "AT" 순서로 딱 맞음)
            super.executeProcess(dhnServer, userid, "MS");
        }
    }

    // ==========================================
    // 🛠️ 부모 숙제(추상 메서드) 완벽 오버라이드
    // ==========================================
    @Override
    protected String getChannelName() {
        return "CMS";
    }

    @Override
    protected String getDbTarget() {
        return this.dbTarget;
    }

    @Override
    protected List<RequestBean> fetchWaitingData(String msgType) {
        List<RequestBean> finalSendList = new ArrayList<>(); // 최종적으로 부모에게 줄 합격 데이터
        List<String> invalidList = new ArrayList<>();        // 형식 오류난 불합격 데이터 (DB 에러 처리용)

        try {
            // 1. 파라미터 세팅
            SQLParameter param = new SQLParameter();
            param.setMsg_table(msgTable);
            param.setMsg_type(msgType);
            param.setDatabase(dbTarget);

            // 2. DB에서 데이터 조회 (날것의 데이터)
            List<RequestBean> rawList = requestService.selectRequests(param);

            if (rawList == null || rawList.isEmpty()) {
                return finalSendList; // 데이터 없으면 바로 빈 리스트 리턴
            }

            ObjectMapper mapper = new ObjectMapper();

            // =========================================================
            // ⭐️ 3. 데이터 제조 및 정제 구역 (형님이 찾으시던 바로 그곳!)
            // =========================================================
            for (RequestBean bean : rawList) {

                // [정제 예시 1] 번호가 없거나 짧으면 컷!
                if (bean.getPhn() == null || bean.getPhn().length() < 10) {
                    invalidList.add(bean.getMsgid());
                    continue;
                }

                // [정제 예시 2] 특정 채널에만 들어가는 하드코딩 값 세팅
                // TODO: 여기서 각 타이명 제조
                if ("AT".equals(msgType)) {
                    bean.setProfile("발송프로필키_하드코딩_또는_변수");
                }

                // [정제 예시 3] 아까 만든 만능 JSON (버튼, 이미지 등) 조립 로직 실행!
                // 정상적으로 조립되면 true가 반환됨
                boolean isGoodData = bean.processJsonPayload(mapper, invalidList);

                // 모든 검문소를 통과한 진짜 A급 데이터만 발송 리스트에 추가!
                if (isGoodData) {
                    finalSendList.add(bean);
                }
            }
            // =========================================================

            // 4. 불량 데이터(Invalid) 짬통 처리 (에러코드 7999 업데이트)
            if (!invalidList.isEmpty()) {
                if (!invalidList.isEmpty()) {
                    Msg_Log ml = new Msg_Log();
                    ml.setMsg_table(msgTable);
                    // ml.setLog_table(logTable); 👈 이 부분 아예 삭제!
                    ml.setStatus("4");
                    ml.setResult_message("(AGENT) 데이터 형식 또는 정제 오류");
                    ml.setCode("7999");

                    // DB 상태값 업데이트 (위에서 오버라이드한 UPDATE문만 실행됨)
                    requestService.updateInvalidData(invalidList, ml);

                    // ⭐️ 파일 텍스트 로그에 확실하게 흔적 남기기
                    log.error("[CMS - {}] 데이터 정제 실패! 발송 제외 처리됨. ({}건) MSG_ID: {}", msgType, invalidList.size(), invalidList);
                    }
            }

        } catch (Exception e) {
            log.error("[CMS - {}] 데이터 조회/정제 중 오류: {}", msgType, e.getMessage());
        }

        // 5. 완벽하게 깎인 데이터만 부모의 executeProcess 로 던져줌 -> API 전송 시작!
        return finalSendList;
    }

    @Override
    protected void updateStatusToSent(List<String> msgIds) {
        try {
            // ⭐️ SQLParameter 로 감싸서 던지기 (에러 완벽 해결!)
            SQLParameter param = new SQLParameter();
            param.setMsg_table(msgTable);
            param.setMsgIds(msgIds);
            param.setDatabase(dbTarget);// 리스트를 담아줌

            // API 전송 성공 시, SMS_STATUS를 1로 변경
            requestService.updateSendComplete(param);
        } catch (Exception e) {
            log.error("[CMS] 상태값 업데이트 오류: {}", e.getMessage());
        }
    }
}