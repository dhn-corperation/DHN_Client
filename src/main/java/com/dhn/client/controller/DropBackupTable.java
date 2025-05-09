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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

@Component
public class DropBackupTable implements ApplicationListener<ContextRefreshedEvent>{

	public static boolean isStart = false;
	private boolean isProc = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	private int dropMonth;
	
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
		String dm = appContext.getEnvironment().getProperty("dhnclient.backup_keep_month");
		param.setDatabase(appContext.getEnvironment().getProperty("dhnclient.database"));
		String dual =  appContext.getEnvironment().getProperty("dhnclient.dual");
		String role = appContext.getEnvironment().getProperty("dhnclient.role");
		
		if(dm != null && ( role != null && role.toUpperCase().equals("MASTER") || role == null) ) {
			dropMonth = Integer.parseInt(dm) + 1;
			
			if(dropMonth > 0)
			{
				isStart = true;
			} else {
				log.info("DropBackupTable 작동 안함.");
				posts.postProcessBeforeDestruction(this, null);
			}
		} else {
			log.info("DropBackupTable 작동 안함.");
			posts.postProcessBeforeDestruction(this, null);
		}
	}
	
	@Scheduled(cron = "0 10 0 1 * *")
	private void SendProcess() {
		if(isStart && !isProc) {
			isProc = true;
			
			try {
				log.info("Backup Table Drop 시작 ");
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
				LocalDateTime now = LocalDateTime.now();
				
				param.setBktable(param.getMsg_table() + "_" + AddDate(now.format(formatter), 0, (dropMonth * -1), 0));
				log.info("Backup Table Drop - Table 명 :  " + param.getBktable());
				
				try {
					log.info("Backup Table Drop - Table drop " );
					reqService.dropBackupTable(param);
				} catch(Exception e) {
					log.info("Backup Table Drop - Error : " + e.toString() );
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("Backup Table Drop error : " + e.toString());
			}
			
			isProc = false;
		}
	}
	
	private static String AddDate(String strDate, int year, int month, int day) throws Exception {
		
        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat rtFormat = new SimpleDateFormat("yyyyMM");
        
		Calendar cal = Calendar.getInstance();
        
		Date dt = dtFormat.parse(strDate);
        
		cal.setTime(dt);
        
		cal.add(Calendar.YEAR,  year);
		cal.add(Calendar.MONTH, month);
		cal.add(Calendar.DATE,  day);
        
		return rtFormat.format(cal.getTime());
	}
}

