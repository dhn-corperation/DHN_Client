package com.dhn.client.service;

import com.dhn.client.bean.*;
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
    public List<TmplData> selectTmplData(SQLParameter param) throws Exception {
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

    @Override
    public int selectInsRequestCount(SQLParameter param) throws Exception {
        return templateReqDAO.selectInsRequestCount(param);
    }

    @Override
    public List<TmplData> selectTmplInsData(SQLParameter param) throws Exception {
        return templateReqDAO.selectTmplInsData(param);
    }

    @Override
    public void updateTmplInsAPR(SQLParameter param) throws Exception {
        templateReqDAO.updateTmplInsAPR(param);
    }

    @Override
    public void updateTmplInsREJ(SQLParameter param) throws Exception {
        templateReqDAO.updateTmplInsREJ(param);
    }

    @Override
    public int selectRefreshTmplCount(SQLParameter param) throws Exception {
        return templateReqDAO.selectRefreshTmplCount(param);
    }

    @Override
    public List<TmplData> selectTmplRefreshData(SQLParameter param) throws Exception {
        return templateReqDAO.selectTmplRefreshData(param);
    }

    @Override
    public void updateTmplRefresh(SQLParameter param) throws Exception {
        templateReqDAO.updateTmplRefresh(param);
    }

    @Override
    public void updateTmplrefreshfail(SQLParameter param) throws Exception {
        templateReqDAO.updateTmplrefreshfail(param);
    }

    @Override
    public void selectInsertComments(SQLParameter param) throws Exception {
        templateReqDAO.selectInsertComments(param);
    }

    @Override
    public List<TmplCommentBean> tmplCommentSelect(SQLParameter param) throws Exception {
        return templateReqDAO.tmplCommentSelect(param);
    }

    @Override
    public void selectUpdateComments(SQLParameter param) throws Exception {
        templateReqDAO.selectUpdateComments(param);
    }
}
