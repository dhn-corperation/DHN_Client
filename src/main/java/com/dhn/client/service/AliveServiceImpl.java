package com.dhn.client.service;

import com.dhn.client.bean.AliveData;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.dao.AliveDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AliveServiceImpl implements AliveService {

    @Autowired
    private AliveDAO aliveDAO;

    @Override
    public int selectAliveCount(SQLParameter param) throws Exception {
        return aliveDAO.selectAliveCount(param);
    }

    @Override
    public void aliveInsertData(SQLParameter param) throws Exception {
        aliveDAO.aliveInsertData(param);
    }

    @Override
    public AliveData selectAliveData(SQLParameter param) throws Exception {
        return aliveDAO.selectAliveData(param);
    }

    @Override
    public void aliveUpdateDate(SQLParameter param) throws Exception {
        aliveDAO.aliveUpdateDate(param);
    }

    @Override
    public void aliveUpdateAgent(SQLParameter param) throws Exception {
        aliveDAO.aliveUpdateAgent(param);
    }

}
