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
public class BackupReplace implements ApplicationListener<ContextRefreshedEvent>{

	public static boolean isStart = false;
	private boolean isProc = false;
	private SQLParameter param = new SQLParameter();
	private String dhnServer;
	private String userid;
	private String replace_month;
	private String tablebackup;
	
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
		replace_month = appContext.getEnvironment().getProperty("dhnclient.phone_no_replace_month");
		tablebackup = appContext.getEnvironment().getProperty("dhnclient.tablebackup");
		param.setDatabase(appContext.getEnvironment().getProperty("dhnclient.database"));
		String dual =  appContext.getEnvironment().getProperty("dhnclient.dual");
		String role = appContext.getEnvironment().getProperty("dhnclient.role");

		if(replace_month != null && Integer.valueOf(replace_month) > 0 && ( role != null && role.toUpperCase().equals("MASTER") || role == null) )
		{
			isStart = true;
		} else {
			log.info("BackupReplace 작동 안함.");
			posts.postProcessBeforeDestruction(this, null);
		}
		
	}
	
	@Scheduled(cron = "0 20 0 * * *")
	private void ReplaceProcess() {
		if(isStart && !isProc) {
			isProc = true;
			
			try {
				log.info("대상 Table 찾기 ");
				if(tablebackup != null && tablebackup.equals("Y")) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
					LocalDateTime now = LocalDateTime.now();
					now.minusMonths(Integer.valueOf( replace_month ));
					now.minusDays(1);
					
					param.setBktable(param.getMsg_table() + "_" + now.format(formatter));
				} else {
					param.setBktable(param.getMsg_table());
				}
				
				log.info("전화번호 수정할 Table 명 :  " + param.getBktable());
				try {
					log.info("대상 Table 조회 " );
					reqService.checkBackupTable(param);
					
					log.info("대상 Table 조회 성공 -  수정 시작" );
					DateTimeFormatter sndFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
					LocalDateTime snd_dttm = LocalDateTime.now();
					snd_dttm.minusMonths(Integer.valueOf( replace_month ));
					param.setSnd_dttm(snd_dttm.format(sndFormatter));
					reqService.BT_PH_Replace(param);
					
				} catch(Exception e) {
					log.info("Table 없음" );
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				log.error("Table backup error : " + e.toString());
			}
			
			isProc = false;
		}
	}
}

