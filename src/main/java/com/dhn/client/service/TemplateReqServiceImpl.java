package com.dhn.client.service;

import com.dhn.client.bean.ButtonBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.bean.TmplData;
import com.dhn.client.bean.TmplRequestBean;
import com.dhn.client.dao.TemplateReqDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TemplateReqServiceImpl implements TemplateReqSevice{

    @Autowired
    private TemplateReqDAO templateReqDAO;

    @Override
    public int selectTmplRequestCount(SQLParameter param) throws Exception {
        return templateReqDAO.selectTmplRequestCount(param);
    }

    @Override
    public TmplData selectTmplData(SQLParameter param) throws Exception {
        return templateReqDAO.selectTmplData(param);
    }

    @Override
    public List<ButtonBean> selectBtnList(SQLParameter param) throws Exception {
        return templateReqDAO.selectBtnList(param);
    }

    @Override
    public void updateTmplfail(SQLParameter param) throws Exception {
        templateReqDAO.updateTmplfail(param);
    }

    @Override
    public void updateTmplSuccess(SQLParameter param) throws Exception {
        templateReqDAO.updateTmplSuccess(param);
    }
}
