package com.dhn.client.dao;

import com.dhn.client.bean.AliveStatusBean;
import com.dhn.client.bean.SQLParameter;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AliveDAOImpl implements AliveDAO{

    @Autowired
    private SqlSession sqlSession;

    @Override
    public int AliveCount(SQLParameter param) throws Exception {
        int cnt = sqlSession.selectOne("com.dhn.client.alive.mapper.SendRequest.alive_count", param);
        return 0;
    }

    @Override
    public void AliveInsert(SQLParameter param) throws Exception {
        sqlSession.insert("com.dhn.client.alive.mapper.SendRequest.alive_insert", param);
    }

    @Override
    public AliveStatusBean getAliveStatus(SQLParameter param) throws Exception {
        AliveStatusBean _as;

        _as = sqlSession.selectOne("com.dhn.client.alive.mapper.SendRequest.alive_status", param);

        return _as;
    }

    @Override
    public void AliveUpdate(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.alive.mapper.SendRequest.alive_update", param);
    }
}
