package com.dhn.client.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@MapperScan(value = {"com.dhn.client.*.mapper.SendRequest",
		"com.dhn.client.mapper"}, sqlSessionFactoryRef = "sqlSessionFactory")
@EnableTransactionManagement
public class DatabaseConfig {

	@Autowired
	private ApplicationContext applicationContext;

	// ==========================================
	// 1. 단일 실물 DataSource 생성 (spring.datasource 바로 매핑)
	// ==========================================
	@Primary
	@Bean(name = "dataSource")
	@ConfigurationProperties(prefix = "spring.datasource.database")
	public DataSource dataSource() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}

	// ==========================================
	// 2. MyBatis 세팅
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
		sqlSessionFactoryBean.setDataSource(dataSource);
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