package com.dhn.client.dao;

import com.dhn.client.bean.AliveStatusBean;
import com.dhn.client.bean.SQLParameter;

public interface AliveDAO {

    int AliveCount(SQLParameter param) throws Exception;

    void AliveInsert(SQLParameter param) throws Exception;

    AliveStatusBean getAliveStatus(SQLParameter param) throws Exception;

    void AliveUpdate(SQLParameter param) throws Exception;
}
