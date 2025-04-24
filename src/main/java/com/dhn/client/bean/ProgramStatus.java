package com.dhn.client.bean;

import javax.annotation.PostConstruct;

import com.dhn.client.controller.AliveMonitoring;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import com.dhn.client.controller.MMSSendRequest;
import com.dhn.client.controller.ResultReq;
import com.dhn.client.controller.SMSSendRequest;

@Service
@Slf4j
public class ProgramStatus implements CommandLineRunner, ApplicationListener<ContextClosedEvent> , DisposableBean {
	
    @Override
    public void run(String... args) throws Exception {
        log.info("프로그램이 정상적으로 실행 되었습니다.");
    }
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
    	ResultReq.isStart = false;
    	SMSSendRequest.isStart = false;
    	MMSSendRequest.isStart = false;
        AliveMonitoring.isStart = false;
    	log.info("프로그램이 종료 처리 중.....");
    	try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
 
    @Override
    public void destroy() throws Exception {
    	log.info("프로그램이 정상적으로 종료 되었습니다.");
    }
}
