package com.dhn.client;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class StartupInitializer {
    @PostConstruct
    public void init() {
        // 애플리케이션 시작 시 main으로 설정
        DynamicRoutingDataSource.setContext("main");
    }
}
