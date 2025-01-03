package com.dhn.client.bean;

import lombok.Data;

import java.util.List;

@Data
public class TmplRequestBean {

    private String senderKey;
    private String templateCode;
    private String templateName;
    private String templateMessageType;
    private String templateEmphasizeType;
    private String templateContent;
    private List<ButtonBean> buttons;
}
