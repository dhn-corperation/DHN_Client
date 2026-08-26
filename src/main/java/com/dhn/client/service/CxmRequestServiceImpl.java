package com.dhn.client.service;

import com.dhn.client.dao.AbstractRequestDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("cxmService")
public class CxmRequestServiceImpl extends AbstractRequestServiceImpl {

    @Autowired
    @Qualifier("cxmDao")
    private AbstractRequestDAO cxmDao;

    @Override
    protected AbstractRequestDAO getDao() {
        return this.cxmDao;
    }
}