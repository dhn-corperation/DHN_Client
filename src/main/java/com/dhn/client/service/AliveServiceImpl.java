package com.dhn.client.service;

import com.dhn.client.bean.AliveStatusBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.dao.AliveDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AliveServiceImpl implements AliveService{

    @Autowired
    private AliveDAO aliveDAO;


    @Override
    public int AliveCount(SQLParameter param) throws Exception {
        return aliveDAO.AliveCount(param);
    }

    @Override
    public void AliveInsert(SQLParameter param) throws Exception {
        aliveDAO.AliveInsert(param);
    }

    @Override
    public AliveStatusBean getAliveStatus(SQLParameter param) throws Exception {
        return aliveDAO.getAliveStatus(param);
    }

    @Override
    public void AliveUpdate(SQLParameter param) throws Exception {
        aliveDAO.AliveUpdate(param);
    }
}
