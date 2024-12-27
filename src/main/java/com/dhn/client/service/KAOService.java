package com.dhn.client.service;

import com.dhn.client.AES256_GCM;
import com.dhn.client.bean.ButtonJsonBean;
import com.dhn.client.bean.KAORequestBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;

@Service
@Slf4j
public class KAOService {
	
	@Autowired
	private AES256_GCM aes256;
	
	public KAORequestBean encryption(KAORequestBean kaoRequestBean, String column) {
		GCMParameterSpec nonce = generateGCMParameterSpec();
		//String noncestd = Base64.getEncoder().encodeToString(nonce.getIV());
		String nonceHex = aes256.toHex(nonce.getIV());
		kaoRequestBean.setCrypto(nonceHex+","+column);
		try {
			
			if(column.toUpperCase().contains("PHN") && kaoRequestBean.getPhn() != null && !kaoRequestBean.getPhn().isEmpty()) {
				kaoRequestBean.setPhn(aes256.encrypt(kaoRequestBean.getPhn(), nonce));
			}
			if(column.toUpperCase().contains("MSG") && kaoRequestBean.getMsg() != null && !kaoRequestBean.getMsg().isEmpty()) {
				kaoRequestBean.setMsg(aes256.encrypt(kaoRequestBean.getMsg(), nonce));
			}
			if(column.toUpperCase().contains("MESSAGETYPE") && kaoRequestBean.getMessagetype() != null && !kaoRequestBean.getMessagetype().isEmpty()) {
				kaoRequestBean.setMessagetype(aes256.encrypt(kaoRequestBean.getMessagetype(), nonce));
			}
			if(column.toUpperCase().contains("MSGSMS") && kaoRequestBean.getMsgsms() != null && !kaoRequestBean.getMsgsms().isEmpty()) {
				kaoRequestBean.setMsgsms(aes256.encrypt(kaoRequestBean.getMsgsms(), nonce));
			}
			if(column.toUpperCase().contains("SMSSENDER") && kaoRequestBean.getSmssender() != null && !kaoRequestBean.getSmssender().isEmpty()) {
				kaoRequestBean.setSmssender(aes256.encrypt(kaoRequestBean.getSmssender(), nonce));
			}
			if(column.toUpperCase().contains("TMPLID") && kaoRequestBean.getTmplid() != null && !kaoRequestBean.getTmplid().isEmpty()) {
				kaoRequestBean.setTmplid(aes256.encrypt(kaoRequestBean.getTmplid(), nonce));
			}
			if(column.toUpperCase().contains("SMSLMSTIT") && kaoRequestBean.getSmslmstit() != null && !kaoRequestBean.getSmslmstit().isEmpty()) {
				kaoRequestBean.setSmslmstit(aes256.encrypt(kaoRequestBean.getSmslmstit(), nonce));
			}
		}catch (Exception e) {
			log.info("데이터 암호화 오류 : ",e.getMessage());
		}
		return kaoRequestBean;
	}

	public KAORequestBean Btn_form(KAORequestBean kaoRequestBean) {

		String[] buttons = kaoRequestBean.getButton1().split("\\|");

		if (buttons.length > 0) {
			kaoRequestBean.setButton1(Btn_json(buttons[0],kaoRequestBean));
		}
		if (buttons.length > 1) {
			kaoRequestBean.setButton2(Btn_json(buttons[1],kaoRequestBean));
		}
		if (buttons.length > 2) {
			kaoRequestBean.setButton3(Btn_json(buttons[2],kaoRequestBean));
		}
		if (buttons.length > 3) {
			kaoRequestBean.setButton4(Btn_json(buttons[3],kaoRequestBean));
		}
		if (buttons.length > 4) {
			kaoRequestBean.setButton5(Btn_json(buttons[4],kaoRequestBean));
		}

		return kaoRequestBean;

	}

	private String Btn_json(String btn, KAORequestBean kaoRequestBean) {

		String[] buttons = btn.split("\\^");

		ButtonJsonBean btnjb = new ButtonJsonBean();
		btnjb.setName(buttons[0]);
		btnjb.setType(buttons[1]);
		btnjb.setUrl_mobile(buttons[2]);
		btnjb.setUrl_pc(buttons[3]);

		String jsonString = "";

		//kaoRequestBean.addUrlToMsgsms(buttons[3]);

		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonString = mapper.writeValueAsString(btnjb);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonString;
	}

	 public GCMParameterSpec generateGCMParameterSpec() {
	        byte[] iv = new byte[12]; // 12바이트 nonce
	        new SecureRandom().nextBytes(iv);
	        return new GCMParameterSpec(128, iv);
	   }
}
