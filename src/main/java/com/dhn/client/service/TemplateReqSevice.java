package com.dhn.client.service;

import com.dhn.client.bean.SQLParameter;

public interface TemplateReqSevice {
    int selectTmpRequestCount(SQLParameter param) throws Exception;
}
