package com.dhn.client.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL) // null 인 필드는 API JSON 변환 시 자동 제외!
public class RequestBean {

    // ==========================================
    // 1. 외부 API 전송 및 공통 필드
    // ==========================================
    private String msgid;
    private String adflag;
    private String button1;
    private String button2;
    private String button3;
    private String button4;
    private String button5;
    private String imagelink;
    private String imageurl;
    private String messagetype;
    private String msg;
    private String msgsms;
    private String onlysms;
    private String pcom;
    private String pinvoice;
    private String phn;
    private String profile;
    private String regdt;
    private String remark1;
    private String remark2;
    private String remark3;
    private String remark4;
    private String remark5;
    private String reservedt;
    private String scode;
    private String smskind;
    private String smslmstit;
    private String smssender;
    private String tmplid;
    private String wide;
    private String supplement;
    private String price;
    private String currencytype;
    private String title;
    private String header;
    private String att_items;
    private String att_coupon;
    private String crypto;
    private String btn_name;
    private String btn_url;
    private String button;
    private String realsendflag;
    private String link;
    private String kisacode;
    private String kind;
    private String attitems;
    private String pushalarm;
    private int expectedbroadcastcnt;

    // ==========================================
    // 2. 최종 조립되는 JSON 결과 필드 (API 전송용)
    // ==========================================
    private String attachments; // 조립된 attachments JSON 문자열
    private String carousel;    // 조립된 carousel JSON 문자열

    // ==========================================
    // 3. DB에서 읽어오는 RAW JSON 조각들
    // ⭐️ @JsonIgnore: API 호출 시 JSON 바디에 포함되지 않도록 숨김!
    // ==========================================
    @JsonIgnore private String attimage;
    @JsonIgnore private String attbutton;
    @JsonIgnore private String attitem;
    @JsonIgnore private String attcoupon;
    @JsonIgnore private String attcommerce;
    @JsonIgnore private String attvideo;

    @JsonIgnore private String carhead;
    @JsonIgnore private String carlist;
    @JsonIgnore private String cartail;


    /**
     * 💡 DB Raw 데이터들을 검증하고 attachments / carousel 로 자동 조립하는 메서드
     * @return true: 정상 조립 / false: Invalid JSON 데이터
     */
    public boolean processJsonPayload(ObjectMapper mapper, List<String> invalidList) {
        try {
            // ----- 1. attachments 조립 -----
            ObjectNode attNode = mapper.createObjectNode();
            if (processJsonNode(attNode, "image", this.attimage, mapper)) { invalidList.add(this.msgid); return false; }
            if (processJsonNode(attNode, "button", this.attbutton, mapper)) { invalidList.add(this.msgid); return false; }
            if (processJsonNode(attNode, "item", this.attitem, mapper)) { invalidList.add(this.msgid); return false; }
            if (processJsonNode(attNode, "coupon", this.attcoupon, mapper)) { invalidList.add(this.msgid); return false; }
            if (processJsonNode(attNode, "commerce", this.attcommerce, mapper)) { invalidList.add(this.msgid); return false; }
            if (processJsonNode(attNode, "video", this.attvideo, mapper)) { invalidList.add(this.msgid); return false; }

            if (attNode.size() > 0) {
                this.attachments = mapper.writeValueAsString(attNode);
            }

            // ----- 2. carousel 조립 -----
            ObjectNode carNode = mapper.createObjectNode();
            if (processJsonNode(carNode, "head", this.carhead, mapper)) { invalidList.add(this.msgid); return false; }
            if (processJsonNode(carNode, "list", this.carlist, mapper)) { invalidList.add(this.msgid); return false; }
            if (processJsonNode(carNode, "tail", this.cartail, mapper)) { invalidList.add(this.msgid); return false; }

            if (carNode.size() > 0) {
                this.carousel = mapper.writeValueAsString(carNode);
            }

            return true; // 정상 처리 완료
        } catch (Exception e) {
            log.error("RequestBean JSON 조립 실패 msgid={}", this.msgid, e);
            invalidList.add(this.msgid);
            return false;
        }
    }

    private boolean processJsonNode(ObjectNode parentNode, String key, String jsonStr, ObjectMapper mapper) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) return false;

        try {
            JsonNode node = mapper.readTree(jsonStr);
            if (node.isArray()) {
                if (node.size() > 0) parentNode.set(key, node);
                return false;
            }
            if (node.isObject()) {
                if (node.fieldNames().hasNext()) parentNode.set(key, node);
                return false;
            }
            return true; // Invalid
        } catch (Exception e) {
            return true; // Invalid JSON Format
        }
    }
}