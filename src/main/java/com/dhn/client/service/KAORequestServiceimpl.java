package com.dhn.client.service;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.dao.KAORequestDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class KAORequestServiceimpl implements KAORequestService{

    @Autowired
    private KAORequestDAO kaoRequestDAO;

    @Override
    public int selectKAORequestCount(SQLParameter param) throws Exception {
        return kaoRequestDAO.selectKAORequestCount(param);
    }

    @Override
    public void updateKAOGroupNo(SQLParameter param) throws Exception {
        kaoRequestDAO.updateKAOGroupNo(param);
    }

    @Override
    public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {
        return kaoRequestDAO.selectKAORequests(param);
    }

    @Override
    public void updateKAOSendComplete(SQLParameter param) throws Exception {
        kaoRequestDAO.updateKAOSendComplete(param);
    }

    @Override
    public void updateKAOSendInit(SQLParameter param) throws Exception {
        kaoRequestDAO.updateKAOSendInit(param);
    }


}
