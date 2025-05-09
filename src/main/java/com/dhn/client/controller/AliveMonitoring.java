package com.dhn.client.controller;


import com.dhn.client.bean.AliveStatusBean;
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

@Component
public class AliveMonitoring implements ApplicationListener<ContextRefreshedEvent>{

	public static boolean isStart = false;
	private boolean isProc = false;
	private String dual;
	private String role;
	private String database;
	private String otp;
	
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
		database =  appContext.getEnvironment().getProperty("dhnclient.database");
		dual =  appContext.getEnvironment().getProperty("dhnclient.dual");
		role = appContext.getEnvironment().getProperty("dhnclient.role");
		otp =  appContext.getEnvironment().getProperty("dhnclient.use_otp");
		
		if(dual != null) {
			if(dual.toUpperCase().equals("Y"))
			{
				isStart = true;
			} else {
				posts.postProcessBeforeDestruction(this, null);
			}
		} else {
			posts.postProcessBeforeDestruction(this, null);
		}
	}
	
	@Scheduled(fixedDelay = 10000)
	private void MonitoringProcess() {
		if(isStart && !isProc) {
			isProc = true;
			
			try {

				SQLParameter param = new SQLParameter();
				param.setRole(role);
				param.setDatabase(database);
				
				// Role 를 구분하여 DB 감시 시작
				if( role != null && role.toUpperCase().equals("MASTER")) {
					int cnt = reqService.AliveCount(param);
					
					if(cnt == 0) {
						reqService.AliveInsert(param);
					}

					AliveStatusBean _as = reqService.getAliveStatus(param);
					if(_as.getStatus() != null && _as.getStatus().toUpperCase().equals("MS")) {
						param.setAlive_status(_as.getStatus().toUpperCase());
						if(_as.getRole().toUpperCase().equals("MASTER")) {
							
							reqService.AliveUpdate(param);	
							if(!KAOSendRequest.isStart || !LMSSendRequest.isStart || !MMSSendRequest.isStart || !SMSSendRequest.isStart || !ResultReq.isStart) {
								KAOSendRequest.setIsStart(true);
								MMSSendRequest.setIsStart(true);
								LMSSendRequest.setIsStart(true);
								SMSSendRequest.setIsStart(true);
								ResultReq.setIsStart( true);	
								if(otp != null && otp.toUpperCase().equals("Y"))
								{
									OTPSendRequest.setIsStart(true);
								}
							} 

							reqService.AliveUpdate(param);
				
						} else {
							KAOSendRequest.setIsStart(true);
							MMSSendRequest.setIsStart(true);
							LMSSendRequest.setIsStart(true);
							SMSSendRequest.setIsStart(true);
							ResultReq.setIsStart( true);
							if(otp != null && otp.toUpperCase().equals("Y"))
							{
								OTPSendRequest.setIsStart(true);
							}
							
							reqService.AliveUpdate(param);	
						}
						
					} else if(_as.getStatus() != null && _as.getStatus().toUpperCase().equals("SS")) {
						param.setAlive_status("MW");
						reqService.AliveUpdate(param);
					}
					
					
					
				} else if( role != null && role.toUpperCase().equals("SLAVE")) {
					int cnt = reqService.AliveCount(param);
					
					if(cnt > 0) {
						int isAlive = reqService.AliveLastCount(param);
						if(isAlive == 0) {
							AliveStatusBean _as = reqService.getAliveStatus(param);
							if(_as.getRole().toUpperCase().equals("MASTER") && _as.getStatus().toUpperCase().equals("MS")) {
								KAOSendRequest.setIsStart(true);
								MMSSendRequest.setIsStart(true);
								LMSSendRequest.setIsStart(true);
								SMSSendRequest.setIsStart(true);
								ResultReq.setIsStart( true);	
								if(otp != null && otp.toUpperCase().equals("Y"))
								{
									OTPSendRequest.setIsStart(true);
								}
								param.setAlive_status("SS");
								reqService.AliveUpdate(param);
								reqService.AliveAlarmInsert(param);
							} else if(_as.getStatus().toUpperCase().equals("MW")) {
								KAOSendRequest.setIsStart(false);
								MMSSendRequest.setIsStart(false);
								LMSSendRequest.setIsStart(false);
								SMSSendRequest.setIsStart(false);
								ResultReq.setIsStart( false);
								if(otp != null && otp.toUpperCase().equals("Y"))
								{
									OTPSendRequest.setIsStart(false);
								}
								if(!KAOSendRequest.isProc && !LMSSendRequest.isProc && !MMSSendRequest.isProc && !SMSSendRequest.isProc && !ResultReq.isProc) {
									param.setAlive_status("MS");
									reqService.AliveUpdate(param);
								}
							}
						}
					}
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("Alive checked error : " + role);
			}
			
			isProc = false;
		}
	}
	 
}

