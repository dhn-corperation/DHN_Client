package com.dhn.client.service;

import com.dhn.client.dao.AbstractRequestDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("rmsService")
public class RmsRequestServiceImpl extends AbstractRequestServiceImpl {

    @Autowired
    @Qualifier("rmsDao")
    private AbstractRequestDAO rmsDao;

    @Override
    protected AbstractRequestDAO getDao() {
        return this.rmsDao;
    }
}