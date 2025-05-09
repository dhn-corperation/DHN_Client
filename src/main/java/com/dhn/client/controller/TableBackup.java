package com.dhn.client.controller;


import com.dhn.client.bean.SQLParameter;
import com.dhn.client.service.RequestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TableBackup implements ApplicationListener<ContextRefreshedEvent>{

	public static boolean isStart = false;
	private boolean isProc = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	private String dual;
	private static String role;
	
	private static final Logger log = LogManager.getRootLogger();
	
	@Autowired
	private RequestService reqService;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Autowired
	ScheduledAnnotationBeanPostProcessor posts;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// TODO Auto-generated method stub
		param.setMsg_table( appContext.getEnvironment().getProperty("dhnclient.msg_table") );
		String tablebackup = appContext.getEnvironment().getProperty("dhnclient.tablebackup");
		param.setDatabase(appContext.getEnvironment().getProperty("dhnclient.database"));
		dual =  appContext.getEnvironment().getProperty("dhnclient.dual");
		role = appContext.getEnvironment().getProperty("dhnclient.role");
		
		if(tablebackup != null && tablebackup.toUpperCase().equals("Y") && ( role != null && role.toUpperCase().equals("MASTER") || role == null) )
		{
			isStart = true;
		} else {
			log.info("TableBackup 작동 안함.");
			posts.postProcessBeforeDestruction(this, null);
		}
		
	}
	
	@Scheduled(cron = "0 5 0 * * *")
	private void SendProcess() {
		if(isStart && !isProc) {
			isProc = true;
			
			try {
				log.info("Table backup 시작 ");
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
				LocalDateTime now = LocalDateTime.now();
				param.setBktable(param.getMsg_table() + "_" + now.format(formatter));
				log.info("Table backup - Table 명 :  " + param.getBktable());
				try {
					log.info("Table backup - Table 조회 " );
					reqService.checkBackupTable(param);
				} catch(Exception e) {
					log.info(e.toString());
					log.info("Table backup - Table 생성 시작 " );
					reqService.createBackupTable(param);
					log.info("Table backup - Table 생성 끝 " );
				}
				log.info("Table backup - Table 복사 시작" );
				reqService.moveBackupTable(param);
				log.info("Table backup - Table 복사 끝" );
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				log.error("Table backup error : " + e.toString());
			}
			
			isProc = false;
		}
	}
}

