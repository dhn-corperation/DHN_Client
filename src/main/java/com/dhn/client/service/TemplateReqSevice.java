package com.dhn.client.service;

import com.dhn.client.bean.ButtonBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.bean.TmplData;
import com.dhn.client.bean.TmplRequestBean;

import java.util.List;

public interface TemplateReqSevice {

    int selectTmplRequestCount(SQLParameter param) throws Exception;

    List<TmplData> selectTmplData(SQLParameter param) throws Exception;

    List<ButtonBean> selectBtnList(SQLParameter param) throws Exception;

    void updateTmplfail(SQLParameter param) throws Exception;

    void updateTmplSuccess(SQLParameter param) throws Exception;

    int selectInsRequestCount(SQLParameter param) throws Exception;

    List<TmplData> selectTmplInsData(SQLParameter param) throws Exception;

    void updateTmplInsAPR(SQLParameter param) throws Exception;

    void updateTmplInsREJ(SQLParameter param) throws Exception;

    int selectRefreshTmplCount(SQLParameter param) throws Exception;

    List<TmplData> selectTmplRefreshData(SQLParameter param) throws Exception;

    void updateTmplRefresh(SQLParameter param) throws Exception;

    void updateTmplrefreshfail(SQLParameter param) throws Exception;
}
