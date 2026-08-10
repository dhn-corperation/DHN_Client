package com.dhn.client.controller;

import com.dhn.client.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LogTableManager {

    // =========================================================================
    // 🚨 [수정 포인트] 형님이 실제 사용하시는 서비스 빈(변수) 이름으로 맞춰주세요!
    // =========================================================================
    @Autowired
    @Qualifier("cmsService")
    private RequestService cmsService;

    @Autowired
    @Qualifier("cxmService")
    private RequestService cxmService;

    @Autowired
    @Qualifier("erpService")
    private RequestService erpService;

    @Autowired
    @Qualifier("rmsService")
    private RequestService rmsService;

    @Autowired
    @Qualifier("webService")
    private RequestService webService;


    // ==========================================
    // ⭐️ application.yml 설정값 (5개 모듈 전부)
    // ==========================================
    @Value("${dhnclient.cms_use:N}") private String cmsUse;
    @Value("${dhnclient.cms.log_back:N}") private String cmsLogBack;
    @Value("${dhnclient.cms.msg_table:TBL_SUBMIT_QUEUE}")private String cmsMsgTable;
    @Value("${dhnclient.cms.log_table:TBL_MSG_HIST}") private String cmsLogTable;

    @Value("${dhnclient.cxm_use:N}") private String cxmUse;
    @Value("${dhnclient.cxm.log_back:N}") private String cxmLogBack;
    @Value("${dhnclient.cxm.msg_table:EMFO_DATA}") private String cxmMsgTable;
    @Value("${dhnclient.cxm.log_table:EMFO_LOG}") private String cxmLogTable;

    @Value("${dhnclient.erp_use:N}") private String erpUse;
    @Value("${dhnclient.erp.log_back:N}") private String erpLogBack;
    @Value("${dhnclient.erp.msg_table:MTMSG_DATA}")private String erpMsgTable;
    @Value("${dhnclient.erp.log_table:MTMSG_LOG}") private String erpLogTable;

    @Value("${dhnclient.rms_use:N}") private String rmsUse;
    @Value("${dhnclient.rms.log_back:N}") private String rmsLogBack;
    @Value("${dhnclient.rms.msg_table:SUREDATA}") private String rmsMsgTable;
    @Value("${dhnclient.rms.log_table:SUREDATA_LOG}") private String rmsLogTable;

    @Value("${dhnclient.web_use:N}") private String webUse;
    @Value("${dhnclient.web.log_back:N}") private String webLogBack;
    @Value("${dhnclient.web.msg_table:SUREDATA}") private String webMsgTable;
    @Value("${dhnclient.web.log_table:SUREDATA_LOG}") private String webLogTable;


    // ==========================================
    // ⭐️ 스케줄러 로직
    // ==========================================
    @PostConstruct
    public void initOnStartup() {
        log.info("[LogTableManager] 로그 테이블 체크(첫가동)");
        checkAndCreateMonthlyTables();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleNextMonthTables() {
        log.info("[LogTableManager] 로그 테이블 체크");
        checkAndCreateMonthlyTables();
    }

    private void checkAndCreateMonthlyTables() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String nextMonth = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));

        if("Y".equalsIgnoreCase(cmsLogBack)) {
            processModule("CMS", cmsUse, cmsMsgTable, cmsLogTable, cmsService, currentMonth, nextMonth);
        }
        if("Y".equalsIgnoreCase(cxmLogBack)) {
            processModule("CXM", cxmUse, cxmMsgTable, cxmLogTable, cxmService, currentMonth, nextMonth);
        }
        if("Y".equalsIgnoreCase(erpLogBack)) {
            processModule("ERP", erpUse, erpMsgTable, erpLogTable, erpService, currentMonth, nextMonth);
        }
        if("Y".equalsIgnoreCase(rmsLogBack)) {
            processModule("RMS", rmsUse, rmsMsgTable, rmsLogTable, rmsService, currentMonth, nextMonth);
        }
        if("Y".equalsIgnoreCase(webLogBack)) {
            processModule("WEB", webUse, webMsgTable, webLogTable, webService, currentMonth, nextMonth);
        }
    }

    /**
     * ⭐️ 반복되는 검사/생성 로직을 처리하는 헬퍼 메서드
     */
    private void processModule(String moduleName, String use, String msgTable, String logTable, RequestService service, String currentMonth, String nextMonth) {
        // 해당 모듈의 use가 'Y'이고, 테이블명 설정이 비어있지 않을 때만 동작
        if ("Y".equalsIgnoreCase(use) && !msgTable.isEmpty() && !logTable.isEmpty()) {
            try {
                // 이번 달 테이블 점검/생성
                service.logTableCheck(msgTable, logTable + "_" + currentMonth);
                // 다음 달 테이블 점검/생성
                service.logTableCheck(msgTable, logTable + "_" + nextMonth);
            } catch (Exception e) {
                log.error("[LogTableManager] {} 로그 테이블 자동생성 실패: {}", moduleName, e.getMessage());
            }
        }
    }
}