package com.dhn.client.bean;

import lombok.Data;

import java.util.List;

@Data
public class TmplUpdateBean {

    private String senderKey;
    private String templateCode;
    private String newSenderKey;
    private String newTemplateCode;
    private String newTemplateName;
    private String newTemplateMessageType;
    private String newTemplateEmphasizeType;
    private String newTemplateContent;
    private List<ButtonBean> buttons;

}
