package com.dhn.client.service;

import com.dhn.client.AES256_GCM;
import com.dhn.client.bean.PUSHRequestBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;

@Service
@Slf4j
public class PUSHService {

    @Autowired
    private AES256_GCM aes256;

    public PUSHRequestBean encryption(PUSHRequestBean pushRequestBean, String column) {
        GCMParameterSpec nonce = generateGCMParameterSpec();
        //String noncestd = Base64.getEncoder().encodeToString(nonce.getIV());
        String nonceHex = aes256.toHex(nonce.getIV());
        pushRequestBean.setCrypto(nonceHex+","+column);
        try {

            if(column.toUpperCase().contains("PHN") && pushRequestBean.getPhn() != null && !pushRequestBean.getPhn().isEmpty()) {
                pushRequestBean.setPhn(aes256.encrypt(pushRequestBean.getPhn(), nonce));
            }
            if(column.toUpperCase().contains("MSG") && pushRequestBean.getMsg() != null && !pushRequestBean.getMsg().isEmpty()) {
                pushRequestBean.setMsg(aes256.encrypt(pushRequestBean.getMsg(), nonce));
            }
            if(column.toUpperCase().contains("MESSAGETYPE") && pushRequestBean.getMessagetype() != null && !pushRequestBean.getMessagetype().isEmpty()) {
                pushRequestBean.setMessagetype(aes256.encrypt(pushRequestBean.getMessagetype(), nonce));
            }
            if(column.toUpperCase().contains("MSGSMS") && pushRequestBean.getMsgsms() != null && !pushRequestBean.getMsgsms().isEmpty()) {
                pushRequestBean.setMsgsms(aes256.encrypt(pushRequestBean.getMsgsms(), nonce));
            }
            if(column.toUpperCase().contains("SMSSENDER") && pushRequestBean.getSmssender() != null && !pushRequestBean.getSmssender().isEmpty()) {
                pushRequestBean.setSmssender(aes256.encrypt(pushRequestBean.getSmssender(), nonce));
            }
            if(column.toUpperCase().contains("TMPLID") && pushRequestBean.getTmplid() != null && !pushRequestBean.getTmplid().isEmpty()) {
                pushRequestBean.setTmplid(aes256.encrypt(pushRequestBean.getTmplid(), nonce));
            }
            if(column.toUpperCase().contains("SMSLMSTIT") && pushRequestBean.getSmslmstit() != null && !pushRequestBean.getSmslmstit().isEmpty()) {
                pushRequestBean.setSmslmstit(aes256.encrypt(pushRequestBean.getSmslmstit(), nonce));
            }
            if(column.toUpperCase().contains("PUSHID") && pushRequestBean.getPushid() != null && !pushRequestBean.getPushid().isEmpty()) {
                pushRequestBean.setPushid(aes256.encrypt(pushRequestBean.getPushid(), nonce));
            }
            if(column.toUpperCase().contains("APPKEY") && pushRequestBean.getAppkey() != null && !pushRequestBean.getAppkey().isEmpty()) {
                pushRequestBean.setAppkey(aes256.encrypt(pushRequestBean.getAppkey(), nonce));
            }
            if(column.toUpperCase().contains("APPSECRET") && pushRequestBean.getAppsecret() != null && !pushRequestBean.getAppsecret().isEmpty()) {
                pushRequestBean.setAppsecret(aes256.encrypt(pushRequestBean.getAppsecret(), nonce));
            }
        }catch (Exception e) {
            log.info("데이터 암호화 오류 : ",e.getMessage());
        }
        return pushRequestBean;
    }

    public GCMParameterSpec generateGCMParameterSpec() {
        byte[] iv = new byte[12]; // 12바이트 nonce
        new SecureRandom().nextBytes(iv);
        return new GCMParameterSpec(128, iv);
    }
}
