package com.dhn.client.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@MapperScan(value = {"com.dhn.client.*.mapper.SendRequest",
		"com.dhn.client.mapper"}, sqlSessionFactoryRef = "sqlSessionFactory")
@EnableTransactionManagement
public class DatabaseConfig {

	@Autowired
	private ApplicationContext applicationContext;

	// ==========================================
	// 1. 실물 DataSource 3개 생성 (yml 정보 매핑)
	// ==========================================
	@Bean(name = "oracle1DataSource")
	@ConfigurationProperties(prefix = "spring.datasource.oracle1")
	public DataSource oracle1DataSource() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}

//	@Bean(name = "oracle2DataSource")
//	@ConfigurationProperties(prefix = "spring.datasource.oracle2")
//	public DataSource oracle2DataSource() {
//		return DataSourceBuilder.create().type(HikariDataSource.class).build();
//	}
//
//	@Bean(name = "mssqlDataSource")
//	@ConfigurationProperties(prefix = "spring.datasource.mssql")
//	public DataSource mssqlDataSource() {
//		return DataSourceBuilder.create().type(HikariDataSource.class).build();
//	}

	// ==========================================
	// 2. 동적 라우팅 DataSource 구성
	// ==========================================
	@Bean(name = "routingDataSource")
	public DataSource routingDataSource(
//			@Qualifier("oracle1DataSource") DataSource oracle1,
//			@Qualifier("oracle2DataSource") DataSource oracle2,
//			@Qualifier("mssqlDataSource") DataSource mssql) {
			@Qualifier("oracle1DataSource") DataSource oracle1) {

		RoutingDataSource routingDataSource = new RoutingDataSource();

		Map<Object, Object> dataSourceMap = new HashMap<>();
		dataSourceMap.put("oracle1", oracle1); // yml의 db-target 이름과 매칭될 키값
//		dataSourceMap.put("oracle2", oracle2);
//		dataSourceMap.put("mssql", mssql);

		routingDataSource.setTargetDataSources(dataSourceMap);
		routingDataSource.setDefaultTargetDataSource(oracle1); // 아무 지정이 없을 때 기본값

		return routingDataSource;
	}

	// ==========================================
	// 3. 트랜잭션 꼬임 방지를 위한 Lazy Proxy (매우 중요 ⭐️)
	// ==========================================
	@Primary
	@Bean(name = "dataSource")
	public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
		// 스프링이 트랜잭션 시작할 때 미리 DB 커넥션을 물고 오지 않게 지연시킴
		// 이래야 DbContextHolder에 세팅한 타겟 DB로 정확히 라우팅 됨!
		return new LazyConnectionDataSourceProxy(routingDataSource);
	}

	// ==========================================
	// 4. MyBatis 세팅 (기존 로직 유지, dataSource 파라미터만 @Primary로 들어감)
	// ==========================================
	@Bean
	public org.apache.ibatis.session.Configuration mybatisConfiguration() {
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		configuration.setMapUnderscoreToCamelCase(true);
		return configuration;
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource,
											   org.apache.ibatis.session.Configuration mybatisConfiguration
	) throws Exception {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource); // <- 여기에 Lazy Proxy DataSource가 들어갑니다.
		sqlSessionFactoryBean.setConfiguration(mybatisConfiguration);
		sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:mapper/main/**/*.xml"));

		return sqlSessionFactoryBean.getObject();
	}

	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory ssf) throws Exception {
		return new SqlSessionTemplate(ssf);
	}

	@Bean
	public PlatformTransactionManager transactionManager(DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}
}