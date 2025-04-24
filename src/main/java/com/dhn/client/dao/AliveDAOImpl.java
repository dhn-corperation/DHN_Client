package com.dhn.client.dao;

import com.dhn.client.bean.AliveData;
import com.dhn.client.bean.SQLParameter;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AliveDAOImpl implements AliveDAO {

    @Autowired
    private SqlSession sqlSession;

    @Override
    public int selectAliveCount(SQLParameter param) throws Exception {
        int cnt = 0;
        switch(param.getDBType())
        {
            case "oracle":
                try {
                    cnt = sqlSession.selectOne("com.dhn.client.alive.mapper.SendRequest.select_alive_count", param);
                } catch(Exception ex) {
                    if(ex.getMessage().contains("ORA-00942"))
                    {
                        sqlSession.update("com.dhn.client.alive.mapper.SendRequest.alive_create_table", param);
                        cnt = sqlSession.selectOne("com.dhn.client.alive.mapper.SendRequest.select_alive_count", param);
                    }
                }
                break;
            case "mysql":
                try {
                    cnt = sqlSession.selectOne("com.dhn.client.alive.mapper.SendRequest.select_alive_count_mysql", param);
                } catch(Exception ex) {
                    if(ex.getMessage().contains("doesn't exist"))
                    {
                        sqlSession.update("com.dhn.client.alive.mapper.SendRequest.alive_create_table_mysql", param);
                        cnt = sqlSession.selectOne("com.dhn.client.alive.mapper.SendRequest.select_alive_count_mysql", param);
                    }
                }
                break;
            default:
                cnt = 0;
                break;
        }
        return cnt;
    }

    @Override
    public void aliveInsertData(SQLParameter param) throws Exception {
        switch(param.getDBType())
        {
            case "oracle":
                sqlSession.insert("com.dhn.client.alive.mapper.SendRequest.alive_insert_data", param);
                break;
            case "mysql":
                sqlSession.insert("com.dhn.client.alive.mapper.SendRequest.alive_insert_data_mysql", param);
                break;
        }
    }

    @Override
    public AliveData selectAliveData(SQLParameter param) throws Exception {
        switch(param.getDBType())
        {
            case "oracle":
                return sqlSession.selectOne("com.dhn.client.alive.mapper.SendRequest.select_alive_data", param);

            case "mysql":
                return sqlSession.selectOne("com.dhn.client.alive.mapper.SendRequest.select_alive_data_mysql", param);

            default:
                return null;
        }
    }

    @Override
    public void aliveUpdateDate(SQLParameter param) throws Exception {
        switch(param.getDBType())
        {
            case "oracle":
                sqlSession.update("com.dhn.client.alive.mapper.SendRequest.alive_update_date", param);
                break;
            case "mysql":
                sqlSession.update("com.dhn.client.alive.mapper.SendRequest.alive_update_date_mysql", param);
                break;
        }
    }

    @Override
    public void aliveUpdateAgent(SQLParameter param) throws Exception {
        switch(param.getDBType())
        {
            case "oracle":
                sqlSession.update("com.dhn.client.alive.mapper.SendRequest.alive_update_agent", param);
                break;
            case "mysql":
                sqlSession.update("com.dhn.client.alive.mapper.SendRequest.alive_update_agent_mysql", param);
                break;
        }
    }
}
