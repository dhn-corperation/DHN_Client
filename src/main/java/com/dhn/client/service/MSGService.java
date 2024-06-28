package com.dhn.client.service;

import java.security.SecureRandom;

import javax.crypto.spec.GCMParameterSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dhn.client.AES256_GCM;
import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.RequestBean;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MSGService {
	
	@Autowired
	private AES256_GCM aes256;
	
	public RequestBean encryption(RequestBean requestBean, String column) {
		GCMParameterSpec nonce = generateGCMParameterSpec();
		//String noncestd = Base64.getEncoder().encodeToString(nonce.getIV());
		String nonceHex = aes256.toHex(nonce.getIV());
		requestBean.setCrypto(nonceHex+","+column);
		try {
			
			if (requestBean.getPhn() != null && !requestBean.getPhn().isEmpty()) {
				requestBean.setPhn(aes256.encrypt(requestBean.getPhn(), nonce));
			}

			String columnLowerCase = column.toLowerCase();

			if(columnLowerCase.contains("msg") && requestBean.getMsg() != null && !requestBean.getMsg().isEmpty()) {
				requestBean.setMsg(aes256.encrypt(requestBean.getMsg(), nonce));
				requestBean.setMsgsms(aes256.encrypt(requestBean.getMsgsms(), nonce));
			}
			if(columnLowerCase.contains("messagetype") && requestBean.getMessagetype() != null && !requestBean.getMessagetype().isEmpty()) {
				requestBean.setMessagetype(aes256.encrypt(requestBean.getMessagetype(), nonce));
			}
			if(columnLowerCase.contains("smssender") && requestBean.getSmssender() != null && !requestBean.getSmssender().isEmpty()) {
				requestBean.setSmssender(aes256.encrypt(requestBean.getSmssender(), nonce));
			}
			if(columnLowerCase.contains("smslmstit") && requestBean.getSmslmstit() != null && !requestBean.getSmslmstit().isEmpty()) {
				requestBean.setSmslmstit(aes256.encrypt(requestBean.getSmslmstit(), nonce));
			}
		}catch (Exception e) {
			log.info("데이터 암호화 오류 : ",e.getMessage());
		}
		return requestBean;
	}
	
	public GCMParameterSpec generateGCMParameterSpec() {
        byte[] iv = new byte[12]; // 12바이트 nonce
        new SecureRandom().nextBytes(iv);
        return new GCMParameterSpec(128, iv);
   }

}
