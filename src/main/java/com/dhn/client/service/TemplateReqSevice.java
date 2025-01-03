package com.dhn.client.service;

import com.dhn.client.bean.ButtonBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.bean.TmplData;
import com.dhn.client.bean.TmplRequestBean;

import java.util.List;

public interface TemplateReqSevice {

    int selectTmplRequestCount(SQLParameter param) throws Exception;

    TmplData selectTmplData(SQLParameter param) throws Exception;

    List<ButtonBean> selectBtnList(SQLParameter param) throws Exception;

    void updateTmplfail(SQLParameter param) throws Exception;

    void updateTmplSuccess(SQLParameter param) throws Exception;
}
