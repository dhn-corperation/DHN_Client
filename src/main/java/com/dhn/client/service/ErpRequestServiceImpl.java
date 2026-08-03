package com.dhn.client.service;

import com.dhn.client.dao.AbstractRequestDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("erpService")
public class ErpRequestServiceImpl extends AbstractRequestServiceImpl  {

    @Autowired
    @Qualifier("erpDao") // DAO단에서 @Repository("cmsDao") 로 등록하신 이름
    private AbstractRequestDAO erpDao;

    @Override
    protected AbstractRequestDAO getDao() {
        return this.erpDao;
    }
}
