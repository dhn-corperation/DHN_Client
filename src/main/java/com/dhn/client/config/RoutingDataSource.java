package com.dhn.client.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        // DbContextHolder에 저장된 DB 타겟 이름(예: "oracle1", "mssql")을 꺼내서 해당 DB로 연결!
        return DbContextHolder.getDbTarget();
    }
}