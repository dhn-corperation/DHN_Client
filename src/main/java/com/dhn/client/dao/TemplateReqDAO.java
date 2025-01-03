package com.dhn.client.dao;

import com.dhn.client.bean.SQLParameter;

public interface TemplateReqDAO {
    int selectTmpRequestCount(SQLParameter param) throws Exception;
}
