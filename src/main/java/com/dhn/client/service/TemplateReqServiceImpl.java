package com.dhn.client.service;

import com.dhn.client.bean.SQLParameter;
import com.dhn.client.dao.TemplateReqDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplateReqServiceImpl implements TemplateReqSevice{

    @Autowired
    private TemplateReqDAO templateReqDAO;

    @Override
    public int selectTmpRequestCount(SQLParameter param) throws Exception {
        return templateReqDAO.selectTmpRequestCount(param);
    }
}
