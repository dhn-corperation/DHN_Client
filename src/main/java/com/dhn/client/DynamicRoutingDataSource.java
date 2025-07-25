package com.dhn.client;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    @Getter
    private static volatile String context = "main"; // 전역 공유

    public static void setContext(String key) {
        log.info("DB Change Requested : {}", key);
        context = key;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        log.debug("Current Routing DB : {}", context);
        return context;
    }
}
