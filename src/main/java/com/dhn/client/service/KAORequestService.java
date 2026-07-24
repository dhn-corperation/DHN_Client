package com.dhn.client.service;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.SQLParameter;

import java.util.List;

public interface KAORequestService {

    public int selectKAORequestCount(SQLParameter param) throws Exception;

    public void updateKAOGroupNo(SQLParameter param) throws Exception;

    public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception;

    public void updateKAOSendComplete(SQLParameter param) throws Exception;

    public void updateKAOSendInit(SQLParameter param) throws Exception;

}
