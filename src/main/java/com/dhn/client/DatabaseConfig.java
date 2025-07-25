package com.dhn.client;

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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
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


//	@Bean
//	@ConfigurationProperties(prefix = "spring.datasource.hikari")
//	public DataSource dataSource() {
//		return DataSourceBuilder.create().type(HikariDataSource.class).build();
//	}
//
//	@Bean
//	public org.apache.ibatis.session.Configuration mybatisConfiguration() {
//		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
//		configuration.setMapUnderscoreToCamelCase(true);
//
//		return configuration;
//	}
//
//	@Bean
//	public SqlSessionFactory sqlSessionFactory(DataSource dataSource,
//			org.apache.ibatis.session.Configuration mybatisConfiguration
//			) throws Exception {
//		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
//		sqlSessionFactoryBean.setDataSource(dataSource);
//		sqlSessionFactoryBean.setConfiguration(mybatisConfiguration);
//		sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:mapper/**/*.xml"));
//
//		return sqlSessionFactoryBean.getObject();
//
//	}
//
//	@Bean
//	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory ssf) throws Exception {
//		return new SqlSessionTemplate(ssf);
//	}
//
//	@Bean
//	public PlatformTransactionManager transactionManager(DataSource ds) {
//		return new DataSourceTransactionManager(ds);
//	}

	@Bean(name = "mainDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.main.hikari")
	public DataSource mainDataSource() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}

	@Bean(name = "standbyDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.standby.hikari")
	public DataSource standbyDataSource() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}

	@Bean
	public DataSource routingDataSource(@Autowired @Qualifier("mainDataSource") DataSource mainDataSource,@Autowired @Qualifier("standbyDataSource") DataSource standbyDataSource) {
		Map<Object, Object> targetDataSources = new HashMap<>();
		targetDataSources.put("main", mainDataSource);
		targetDataSources.put("standby", standbyDataSource);

		DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource();
		routingDataSource.setTargetDataSources(targetDataSources);
		routingDataSource.setDefaultTargetDataSource(mainDataSource); // 기본은 메인
		return routingDataSource;
	}

	@Bean
	public org.apache.ibatis.session.Configuration mybatisConfiguration() {
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		configuration.setMapUnderscoreToCamelCase(true);
		return configuration;
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory(@Qualifier("routingDataSource") DataSource routingDataSource,org.apache.ibatis.session.Configuration mybatisConfiguration) throws Exception {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(routingDataSource);
		sqlSessionFactoryBean.setConfiguration(mybatisConfiguration);
		sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:mapper/**/*.xml"));
		return sqlSessionFactoryBean.getObject();
	}

	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	@Bean
	public PlatformTransactionManager transactionManager(@Qualifier("routingDataSource") DataSource routingDataSource) {
		return new DataSourceTransactionManager(routingDataSource);
	}
}
