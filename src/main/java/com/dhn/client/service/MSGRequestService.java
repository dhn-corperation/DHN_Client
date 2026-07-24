package com.dhn.client.service;

import com.dhn.client.bean.ImageBean;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;

import java.util.List;

public interface MSGRequestService {
    // SMS
    public int selectSMSRequestCount(SQLParameter param) throws Exception;

    public void updateSMSGroupNo(SQLParameter param) throws Exception;

    public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception;

    // LMS
    public int selectLMSRequestCount(SQLParameter param) throws Exception;

    public void updateLMSGroupNo(SQLParameter param) throws Exception;

    public List<RequestBean> selectLMSRequests(SQLParameter param) throws Exception;

    // MMS
    public int selectMMSRequestCount(SQLParameter param) throws Exception;

    public void updateMMSGroupNo(SQLParameter param) throws Exception;

    public List<RequestBean> selectMMSRequests(SQLParameter param) throws Exception;

    // MMS Image
    public int selectMMSImageCount(SQLParameter param) throws Exception;

    public void updateMMSImageGroupNo(SQLParameter param) throws Exception;

    public List<ImageBean> selectMMSImage(SQLParameter param) throws Exception;

    public void updateMMSImageGroup(SQLParameter param) throws Exception;

    public void updateMMSImageFail(SQLParameter param) throws Exception;

    // 공통
    public void updateMsgSendComplete(SQLParameter param) throws Exception;

    public void updateMsgSendInit(SQLParameter param) throws Exception;
}
