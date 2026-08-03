package com.dhn.client.service;
import com.dhn.client.dao.AbstractRequestDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("webService")
public class WebRequestServiceImpl extends AbstractRequestServiceImpl {

    @Autowired
    @Qualifier("webDao")
    private AbstractRequestDAO webDao;

    @Override
    protected AbstractRequestDAO getDao() {
        return this.webDao;
    }
}
