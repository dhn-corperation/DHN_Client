package com.dhn.client.controller;

import com.dhn.client.DynamicRoutingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Slf4j
public class DBHealthChecker {

    @Autowired
    @Qualifier("mainDataSource")
    private DataSource mainDataSource;

    @Autowired
    @Qualifier("standbyDataSource")
    private DataSource standbyDataSource;

    @Scheduled(fixedDelay = 10000)
    public void checkDbStatus() {
        String current = DynamicRoutingDataSource.getContext();
        log.info("Current Connection DB : {}", current);

        // 메인 연결 체크
        try (Connection conn = mainDataSource.getConnection()) {
            if (!conn.isClosed()) {
                if (!"main".equals(current)) {
                    log.info("Main DB Connection Success → Change Connection to Main DB");
                    DynamicRoutingDataSource.setContext("main");
                }
                return;
            }
        } catch (Exception e) {
            log.warn("Main DB Connection Fail : {}", e.getMessage());
        }

        // standby 연결 시도
        try (Connection conn = standbyDataSource.getConnection()) {
            if (!conn.isClosed()) {
                if (!"standby".equals(current)) {
                    log.warn("Change Connection to Standby");
                    DynamicRoutingDataSource.setContext("standby");
                }
            }
        } catch (Exception e) {
            log.error("Standby DB Connection Fail : {}", e.getMessage());
        }
    }
}
