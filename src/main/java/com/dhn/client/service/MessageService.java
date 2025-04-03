package com.dhn.client.service;

import com.dhn.client.AES256_GCM;
import com.dhn.client.bean.MessageRequestBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;

@Service
@Slf4j
public class MessageService {

    @Autowired
    private AES256_GCM aes256;

    public MessageRequestBean encryption(MessageRequestBean messageRequestBean, String column){
        GCMParameterSpec nonce = generateGCMParameterSpec();
        //String noncestd = Base64.getEncoder().encodeToString(nonce.getIV());
        String nonceHex = aes256.toHex(nonce.getIV());

        messageRequestBean.setCrypto(nonceHex+","+column);

        try {

            if(column.toUpperCase().contains("PHN") && messageRequestBean.getPhn() != null && !messageRequestBean.getPhn().isEmpty()) {
                messageRequestBean.setPhn(aes256.encrypt(messageRequestBean.getPhn(), nonce));
            }
            if(column.toUpperCase().contains("MSG") && messageRequestBean.getMsg() != null && !messageRequestBean.getMsg().isEmpty()) {
                messageRequestBean.setMsg(aes256.encrypt(messageRequestBean.getMsg(), nonce));
            }
            if(column.toUpperCase().contains("MESSAGETYPE") && messageRequestBean.getMessagetype() != null && !messageRequestBean.getMessagetype().isEmpty()) {
                messageRequestBean.setMessagetype(aes256.encrypt(messageRequestBean.getMessagetype(), nonce));
            }
            if(column.toUpperCase().contains("MSGSMS") && messageRequestBean.getMsgsms() != null && !messageRequestBean.getMsgsms().isEmpty()) {
                messageRequestBean.setMsgsms(aes256.encrypt(messageRequestBean.getMsgsms(), nonce));
            }
            if(column.toUpperCase().contains("SMSSENDER") && messageRequestBean.getSmssender() != null && !messageRequestBean.getSmssender().isEmpty()) {
                messageRequestBean.setSmssender(aes256.encrypt(messageRequestBean.getSmssender(), nonce));
            }
            if(column.toUpperCase().contains("TMPLID") && messageRequestBean.getTmplid() != null && !messageRequestBean.getTmplid().isEmpty()) {
                messageRequestBean.setTmplid(aes256.encrypt(messageRequestBean.getTmplid(), nonce));
            }
            if(column.toUpperCase().contains("SMSLMSTIT") && messageRequestBean.getSmslmstit() != null && !messageRequestBean.getSmslmstit().isEmpty()) {
                messageRequestBean.setSmslmstit(aes256.encrypt(messageRequestBean.getSmslmstit(), nonce));
            }
            if(column.toUpperCase().contains("PUSHID") && messageRequestBean.getPushid() != null && !messageRequestBean.getPushid().isEmpty()) {
                messageRequestBean.setPushid(aes256.encrypt(messageRequestBean.getPushid(), nonce));
            }
            if(column.toUpperCase().contains("APPKEY") && messageRequestBean.getAppkey() != null && !messageRequestBean.getAppkey().isEmpty()) {
                messageRequestBean.setAppkey(aes256.encrypt(messageRequestBean.getAppkey(), nonce));
            }
            if(column.toUpperCase().contains("APPSECRET") && messageRequestBean.getAppsecret() != null && !messageRequestBean.getAppsecret().isEmpty()) {
                messageRequestBean.setAppsecret(aes256.encrypt(messageRequestBean.getAppsecret(), nonce));
            }
        }catch (Exception e) {
            log.info("데이터 암호화 오류 : ",e.getMessage());
        }

        return messageRequestBean;
    }

    public GCMParameterSpec generateGCMParameterSpec() {
        byte[] iv = new byte[12]; // 12바이트 nonce
        new SecureRandom().nextBytes(iv);
        return new GCMParameterSpec(128, iv);
    }
}
