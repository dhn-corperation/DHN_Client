package com.dhn.client.service;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.spec.GCMParameterSpec;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dhn.client.AES256_GCM;
import com.dhn.client.bean.ButtonJsonBean;
import com.dhn.client.bean.KAORequestBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KAOService {
	
	@Autowired
	private AES256_GCM aes256;

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger("com.dhn.client.service.KAOService");
	
	public KAORequestBean encryption(KAORequestBean kaoRequestBean, String column) {
		GCMParameterSpec nonce = generateGCMParameterSpec();
		//String noncestd = Base64.getEncoder().encodeToString(nonce.getIV());
		String nonceHex = aes256.toHex(nonce.getIV());
		kaoRequestBean.setCrypto(nonceHex+","+column);
		try {
			
			if(column.contains("PHN") && kaoRequestBean.getPhn() != null && !kaoRequestBean.getPhn().isEmpty()) {
				kaoRequestBean.setPhn(aes256.encrypt(kaoRequestBean.getPhn(), nonce));
			}
			if(column.contains("MSG") && kaoRequestBean.getMsg() != null && !kaoRequestBean.getMsg().isEmpty()) {
				kaoRequestBean.setMsg(aes256.encrypt(kaoRequestBean.getMsg(), nonce));
			}
			if(column.contains("PROFILE") && kaoRequestBean.getProfile() != null && !kaoRequestBean.getProfile().isEmpty()) {
				kaoRequestBean.setProfile(aes256.encrypt(kaoRequestBean.getProfile(), nonce));
			}
			if (column.contains("BUTTON")) {
				
				if(kaoRequestBean.getButton1() != null && !kaoRequestBean.getButton1().isEmpty()) {
					kaoRequestBean.setButton1(aes256.encrypt(kaoRequestBean.getButton1(), nonce));
				}
				if(kaoRequestBean.getButton2() != null && !kaoRequestBean.getButton2().isEmpty()) {
					kaoRequestBean.setButton2(aes256.encrypt(kaoRequestBean.getButton2(), nonce));					
				}
				if(kaoRequestBean.getButton3() != null && !kaoRequestBean.getButton3().isEmpty()) {
					kaoRequestBean.setButton3(aes256.encrypt(kaoRequestBean.getButton3(), nonce));					
				}
				if(kaoRequestBean.getButton4() != null && !kaoRequestBean.getButton4().isEmpty()) {
					kaoRequestBean.setButton4(aes256.encrypt(kaoRequestBean.getButton4(), nonce));					
				}
				if(kaoRequestBean.getButton5() != null && !kaoRequestBean.getButton5().isEmpty()) {
					kaoRequestBean.setButton5(aes256.encrypt(kaoRequestBean.getButton5(), nonce));					
				}
			}
			if(column.contains("MESSAGETYPE") && kaoRequestBean.getMessagetype() != null && !kaoRequestBean.getMessagetype().isEmpty()) {
				kaoRequestBean.setMessagetype(aes256.encrypt(kaoRequestBean.getMessagetype(), nonce));
			}
			if(column.contains("MSGSMS") && kaoRequestBean.getMsgsms() != null && !kaoRequestBean.getMsgsms().isEmpty()) {
				kaoRequestBean.setMsgsms(aes256.encrypt(kaoRequestBean.getMsgsms(), nonce));
			}
			if(column.contains("SMSSENDER") && kaoRequestBean.getSmssender() != null && !kaoRequestBean.getSmssender().isEmpty()) {
				kaoRequestBean.setSmssender(aes256.encrypt(kaoRequestBean.getSmssender(), nonce));
			}
			if(column.contains("TMPLID") && kaoRequestBean.getTmplid() != null && !kaoRequestBean.getTmplid().isEmpty()) {
				kaoRequestBean.setTmplid(aes256.encrypt(kaoRequestBean.getTmplid(), nonce));
			}
			if(column.contains("SMSLMSTIT") && kaoRequestBean.getSmslmstit() != null && !kaoRequestBean.getSmslmstit().isEmpty()) {
				kaoRequestBean.setSmslmstit(aes256.encrypt(kaoRequestBean.getSmslmstit(), nonce));
			}
		}catch (Exception e) {
			log.info("데이터 암호화 오류 : ",e.getMessage());
		}
		return kaoRequestBean;
	}

    public KAORequestBean Btn_form(KAORequestBean kaoRequestBean) {
        log.info("===== Btn_form 시작 =====");
        log.info("msgid: {}", kaoRequestBean.getMsgid());

        String attachmentJson = kaoRequestBean.getButton1();
        log.info("원본 button1 값: {}", attachmentJson);

        if (attachmentJson == null || attachmentJson.trim().isEmpty()) {
            log.warn("ATTACHMENT가 비어있음 - 버튼 처리 건너뜀");
            log.info("===== Btn_form 종료 (빈 데이터) =====");
            return kaoRequestBean;
        }

        // 원본 버튼 데이터 로깅 (일부만)
        if (attachmentJson.length() > 100) {
            log.info("원본 데이터(첫 100자): {}...", attachmentJson.substring(0, 100));
        } else {
            log.info("원본 데이터: {}", attachmentJson);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(attachmentJson);
            log.info("JSON 파싱 성공 - rootNode 타입: {}", rootNode.getNodeType());

            JsonNode buttonsNode = rootNode.get("button");
            log.info("buttonsNode 존재: {}, isArray: {}", buttonsNode != null,
                    buttonsNode != null ? buttonsNode.isArray() : "N/A");

            if (buttonsNode != null && buttonsNode.isArray()) {
                int buttonCount = Math.min(buttonsNode.size(), 5);
                log.info("발견된 버튼 개수: {}, 처리할 개수: {}", buttonsNode.size(), buttonCount);

                // 각 버튼 상세 로깅
                for (int i = 0; i < buttonCount; i++) {
                    JsonNode buttonNode = buttonsNode.get(i);
                    log.info("버튼[{}]: {}", i, buttonNode.toString());
                }

                for (int i = 0; i < buttonCount; i++) {
                    JsonNode buttonNode = buttonsNode.get(i);
                    String buttonJson = mapper.writeValueAsString(buttonNode);
                    log.info("버튼[{}] JSON 변환 완료 (길이: {})", i, buttonJson.length());

                    // 각 버튼을 해당 필드에 설정
                    switch (i) {
                        case 0:
                            kaoRequestBean.setButton1(buttonJson);
                            log.info("button1 설정 완료");
                            break;
                        case 1:
                            kaoRequestBean.setButton2(buttonJson);
                            log.info("button2 설정 완료");
                            break;
                        case 2:
                            kaoRequestBean.setButton3(buttonJson);
                            log.info("button3 설정 완료");
                            break;
                        case 3:
                            kaoRequestBean.setButton4(buttonJson);
                            log.info("button4 설정 완료");
                            break;
                        case 4:
                            kaoRequestBean.setButton5(buttonJson);
                            log.info("button5 설정 완료");
                            break;
                    }
                }

                // 사용되지 않은 버튼 필드는 null로 설정
                for (int i = buttonCount; i < 5; i++) {
                    switch (i) {
                        case 0:
                            kaoRequestBean.setButton1(null);
                            log.info("button1 null 설정");
                            break;
                        case 1:
                            kaoRequestBean.setButton2(null);
                            log.info("button2 null 설정");
                            break;
                        case 2:
                            kaoRequestBean.setButton3(null);
                            log.info("button3 null 설정");
                            break;
                        case 3:
                            kaoRequestBean.setButton4(null);
                            log.info("button4 null 설정");
                            break;
                        case 4:
                            kaoRequestBean.setButton5(null);
                            log.info("button5 null 설정");
                            break;
                    }
                }

                log.info("버튼 파싱 완료: {}개 버튼 처리됨", buttonCount);
            } else {
                log.warn("ATTACHMENT에 버튼 배열이 없습니다: {}", attachmentJson);
            }

        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
            // JSON 파싱 실패 시 기존 로직 실행 (| 구분자 방식)
            processLegacyButtons(kaoRequestBean, attachmentJson);
        } catch (Exception e) {
            log.error("버튼 처리 중 오류 발생: {}", e.getMessage());
        }

        return kaoRequestBean;
    }

    /**
     * 기존 | 구분자 방식 처리 (하위 호환성)
     */
    private void processLegacyButtons(KAORequestBean kaoRequestBean, String buttonData) {
        if (buttonData.contains("|")) {
            String[] buttons = buttonData.split("\\|");

            if (buttons.length > 0) {
                kaoRequestBean.setButton1(Btn_json(buttons[0]));
            }
            if (buttons.length > 1) {
                kaoRequestBean.setButton2(Btn_json(buttons[1]));
            }
            if (buttons.length > 2) {
                kaoRequestBean.setButton3(Btn_json(buttons[2]));
            }
            if (buttons.length > 3) {
                kaoRequestBean.setButton4(Btn_json(buttons[3]));
            }
            if (buttons.length > 4) {
                kaoRequestBean.setButton5(Btn_json(buttons[4]));
            }
        }
    }

    // 기존 Btn_json 메서드 유지
    private String Btn_json(String btn) {
        String[] buttons = btn.split("\\^");

        ButtonJsonBean btnjb = new ButtonJsonBean();
        btnjb.setName(buttons[0]);
        btnjb.setType(buttons[1]);
        btnjb.setUrl_mobile(buttons[2]);
        btnjb.setUrl_pc(buttons[3]);

        String jsonString = "";

        ObjectMapper mapper = new ObjectMapper();
        try {
            jsonString = mapper.writeValueAsString(btnjb);
        } catch (JsonProcessingException e) {
            log.error("버튼 JSON 변환 오류: {}", e.getMessage());
        }

        return jsonString;
    }
	
	 public GCMParameterSpec generateGCMParameterSpec() {
	        byte[] iv = new byte[12]; // 12바이트 nonce
	        new SecureRandom().nextBytes(iv);
	        return new GCMParameterSpec(128, iv);
	   }
}
