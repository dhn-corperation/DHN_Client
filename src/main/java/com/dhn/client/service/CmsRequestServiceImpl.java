package com.dhn.client.service;

import com.dhn.client.dao.AbstractRequestDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("cmsService")
public class CmsRequestServiceImpl extends AbstractRequestServiceImpl {

    @Autowired
    @Qualifier("cmsDao")
    private AbstractRequestDAO cmsDao;

    @Override
    protected AbstractRequestDAO getDao() {
        return this.cmsDao;
    }
}