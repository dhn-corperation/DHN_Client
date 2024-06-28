package com.dhn.client.service;

import com.dhn.client.AES256_GCM;
import com.dhn.client.bean.KAORequestBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.spec.GCMParameterSpec;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class KAOService {

    @Autowired
    private AES256_GCM aes256;

    public KAORequestBean encryption(KAORequestBean kaoRequestBean, String column) {
        GCMParameterSpec nonce = generateGCMParameterSpec();
        //String noncestd = Base64.getEncoder().encodeToString(nonce.getIV());
        String nonceHex = aes256.toHex(nonce.getIV());
        kaoRequestBean.setCrypto(nonceHex + "," + column);
        try {

            if (kaoRequestBean.getPhn() != null && !kaoRequestBean.getPhn().isEmpty()) {
                kaoRequestBean.setPhn(aes256.encrypt(kaoRequestBean.getPhn(), nonce));
            }

            String columnLowerCase = column.toLowerCase();

            if (columnLowerCase.contains("msg") && kaoRequestBean.getMsg() != null && !kaoRequestBean.getMsg().isEmpty()) {
                kaoRequestBean.setMsg(aes256.encrypt(kaoRequestBean.getMsg(), nonce));
                kaoRequestBean.setMsgsms(aes256.encrypt(kaoRequestBean.getMsgsms(), nonce));
            }
            if (columnLowerCase.contains("profile") && kaoRequestBean.getProfile() != null && !kaoRequestBean.getProfile().isEmpty()) {
                kaoRequestBean.setProfile(aes256.encrypt(kaoRequestBean.getProfile(), nonce));
            }
            if (columnLowerCase.contains("button")) {

                if (kaoRequestBean.getButton1() != null && !kaoRequestBean.getButton1().isEmpty()) {
                    kaoRequestBean.setButton1(aes256.encrypt(kaoRequestBean.getButton1(), nonce));
                }
                if (kaoRequestBean.getButton2() != null && !kaoRequestBean.getButton2().isEmpty()) {
                    kaoRequestBean.setButton2(aes256.encrypt(kaoRequestBean.getButton2(), nonce));
                }
                if (kaoRequestBean.getButton3() != null && !kaoRequestBean.getButton3().isEmpty()) {
                    kaoRequestBean.setButton3(aes256.encrypt(kaoRequestBean.getButton3(), nonce));
                }
                if (kaoRequestBean.getButton4() != null && !kaoRequestBean.getButton4().isEmpty()) {
                    kaoRequestBean.setButton4(aes256.encrypt(kaoRequestBean.getButton4(), nonce));
                }
                if (kaoRequestBean.getButton5() != null && !kaoRequestBean.getButton5().isEmpty()) {
                    kaoRequestBean.setButton5(aes256.encrypt(kaoRequestBean.getButton5(), nonce));
                }
            }
            if (columnLowerCase.contains("messagetype") && kaoRequestBean.getMessagetype() != null && !kaoRequestBean.getMessagetype().isEmpty()) {
                kaoRequestBean.setMessagetype(aes256.encrypt(kaoRequestBean.getMessagetype(), nonce));
            }
            if (columnLowerCase.contains("smssender") && kaoRequestBean.getSmssender() != null && !kaoRequestBean.getSmssender().isEmpty()) {
                kaoRequestBean.setSmssender(aes256.encrypt(kaoRequestBean.getSmssender(), nonce));
            }
            if (columnLowerCase.contains("tmplid") && kaoRequestBean.getTmplid() != null && !kaoRequestBean.getTmplid().isEmpty()) {
                kaoRequestBean.setTmplid(aes256.encrypt(kaoRequestBean.getTmplid(), nonce));
            }
            if (columnLowerCase.contains("smslmstit") && kaoRequestBean.getSmslmstit() != null && !kaoRequestBean.getSmslmstit().isEmpty()) {
                kaoRequestBean.setSmslmstit(aes256.encrypt(kaoRequestBean.getSmslmstit(), nonce));
            }
        } catch (Exception e) {
            log.info("데이터 암호화 오류 : ", e.getMessage());
        }
        return kaoRequestBean;
    }

    public KAORequestBean Btn_form(KAORequestBean kaoRequestBean) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = kaoRequestBean.getButton();

            List<Map<String, Object>> buttons;

            String buttonArrayJson = mapper.readTree(json).get("button").toString();

            if (buttonArrayJson.trim().startsWith("[")) {
                buttons = mapper.readValue(buttonArrayJson, new TypeReference<List<Map<String, Object>>>() {});
            } else {
                Map<String, Object> singleButton = mapper.readValue(buttonArrayJson, new TypeReference<Map<String, Object>>() {});
                buttons = Collections.singletonList(singleButton);
            }

            // Set button1 to button5 fields in kaoRequestBean
            kaoRequestBean.setButton1(buttons.size() > 0 ? mapper.writeValueAsString(buttons.get(0)) : null);
            kaoRequestBean.setButton2(buttons.size() > 1 ? mapper.writeValueAsString(buttons.get(1)) : null);
            kaoRequestBean.setButton3(buttons.size() > 2 ? mapper.writeValueAsString(buttons.get(2)) : null);
            kaoRequestBean.setButton4(buttons.size() > 3 ? mapper.writeValueAsString(buttons.get(3)) : null);
            kaoRequestBean.setButton5(buttons.size() > 4 ? mapper.writeValueAsString(buttons.get(4)) : null);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return kaoRequestBean;

    }


    public GCMParameterSpec generateGCMParameterSpec() {
        byte[] iv = new byte[12]; // 12바이트 nonce
        new SecureRandom().nextBytes(iv);
        return new GCMParameterSpec(128, iv);
    }
}
