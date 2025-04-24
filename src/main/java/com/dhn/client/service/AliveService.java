package com.dhn.client.service;

import com.dhn.client.bean.AliveData;
import com.dhn.client.bean.SQLParameter;

public interface AliveService {

    int selectAliveCount(SQLParameter param) throws Exception;

    void aliveInsertData(SQLParameter param) throws Exception;

    AliveData selectAliveData(SQLParameter param) throws Exception;

    void aliveUpdateDate(SQLParameter param) throws Exception;

    void aliveUpdateAgent(SQLParameter param) throws Exception;
}
