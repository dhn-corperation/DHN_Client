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

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

@Component
public class LogRemove implements ApplicationListener<ContextRefreshedEvent>{

	public static boolean isStart = false;
	private boolean isProc = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	private int logKeepday;
	
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
		String lk = appContext.getEnvironment().getProperty("dhnclient.log_file_keep_day");
		param.setDatabase(appContext.getEnvironment().getProperty("dhnclient.database"));
		String dual =  appContext.getEnvironment().getProperty("dhnclient.dual");
		String role = appContext.getEnvironment().getProperty("dhnclient.role");
		
		if(lk != null && ( role != null && role.toUpperCase().equals("MASTER") || role == null))
		{
			log.info("log_file_keep_day : " + lk);
			logKeepday = Integer.parseInt( lk );
			
			if(logKeepday > 0)
			{
				isStart = true;
			} else {
				log.info("LogRemove 작동 안함.");
				posts.postProcessBeforeDestruction(this, null);
			}
		} else {
			log.info("LogRemove 작동 안함.");
			posts.postProcessBeforeDestruction(this, null);
		}
		
	}
	
	@Scheduled(cron = "0 10 0 * * *")
	private void SendProcess() {
		if(isStart && !isProc) {
			isProc = true;
			
			try {
				log.info("Log file 삭제 시작 ");
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
				LocalDateTime now = LocalDateTime.now();
				
				File file = new File("./logs/");
				
				if(file.exists()) {
					if(file.isDirectory()) {
						File[] files = file.listFiles();
						
						Calendar fileCal = Calendar.getInstance() ;
						
						long todayMil = fileCal.getTimeInMillis() ; 
						long oneDayMil = 24*60*60*1000 ;    
						
						for(int i=0; i<files.length;i++) {
							
							Date fileDate = new Date(files[i].lastModified());
							fileCal.setTime(fileDate);
							long diffMil = todayMil - fileCal.getTimeInMillis() ;
						     
						    int diffDay = (int)(diffMil/oneDayMil) ;
						    //log.info("보관기간 :" + diffDay);
						    if(diffDay > logKeepday && files[i].exists()){
						    	files[i].delete();
						    	log.info("보관기간 만료 Log file 삭제 : " + files[i].getName() + "( " + fileDate.toString() + " )" );
						    }
							
						}
					}
					
				}
				
				log.info("Log file 삭제 끝" );
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				log.error("Table backup error : " + e.toString());
			}
			
			isProc = false;
		}
	}
}

