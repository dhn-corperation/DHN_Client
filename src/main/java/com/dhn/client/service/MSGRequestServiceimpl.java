package com.dhn.client.service;

import com.dhn.client.bean.ImageBean;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.dao.MSGRequestDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MSGRequestServiceimpl implements MSGRequestService {

    @Autowired
    private MSGRequestDAO  msgRequestDAO;

    @Override
    public int selectSMSRequestCount(SQLParameter param) throws Exception {
        return msgRequestDAO.selectSMSRequestCount(param);
    }

    @Override
    public void updateSMSGroupNo(SQLParameter param) throws Exception {
        msgRequestDAO.updateSMSGroupNo(param);
    }

    @Override
    public List<RequestBean> selectSMSRequests(SQLParameter param) throws Exception {
        return msgRequestDAO.selectSMSRequests(param);
    }

    @Override
    public int selectLMSRequestCount(SQLParameter param) throws Exception {
        return msgRequestDAO.selectLMSRequestCount(param);
    }

    @Override
    public void updateLMSGroupNo(SQLParameter param) throws Exception {
        msgRequestDAO.updateLMSGroupNo(param);
    }

    @Override
    public List<RequestBean> selectLMSRequests(SQLParameter param) throws Exception {
        return msgRequestDAO.selectLMSRequests(param);
    }

    @Override
    public int selectMMSRequestCount(SQLParameter param) throws Exception {
        return msgRequestDAO.selectMMSRequestCount(param);
    }

    @Override
    public void updateMMSGroupNo(SQLParameter param) throws Exception {
        msgRequestDAO.updateMMSGroupNo(param);
    }

    @Override
    public List<RequestBean> selectMMSRequests(SQLParameter param) throws Exception {
        return msgRequestDAO.selectMMSRequests(param);
    }

    @Override
    public int selectMMSImageCount(SQLParameter param) throws Exception {
        return msgRequestDAO.selectMMSImageCount(param);
    }

    @Override
    public void updateMMSImageGroupNo(SQLParameter param) throws Exception {
        msgRequestDAO.updateMMSImageGroupNo(param);
    }

    @Override
    public List<ImageBean> selectMMSImage(SQLParameter param) throws Exception {
        return msgRequestDAO.selectMMSImage(param);
    }

    @Override
    public void updateMMSImageGroup(SQLParameter param) throws Exception {
        msgRequestDAO.updateMMSImageGroup(param);
    }

    @Override
    public void updateMMSImageFail(SQLParameter param) throws Exception {
        msgRequestDAO.updateMMSImageFail(param);
    }

    @Override
    public void updateMsgSendComplete(SQLParameter param) throws Exception {
        msgRequestDAO.updateMsgSendComplete(param);
    }

    @Override
    public void updateMsgSendInit(SQLParameter param) throws Exception {
        msgRequestDAO.updateMsgSendInit(param);
    }

}
