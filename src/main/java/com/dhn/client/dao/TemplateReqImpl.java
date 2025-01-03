package com.dhn.client.dao;

import com.dhn.client.bean.SQLParameter;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TemplateReqImpl implements TemplateReqDAO{

    @Autowired
    private SqlSession sqlSession;

    @Override
    public int selectTmpRequestCount(SQLParameter param) throws Exception {
        int cnt = 0;
        cnt = sqlSession.selectOne("com.dhn.client.tmpl.mapper.SendRequest.req_tmpl_count",param);
        return cnt;
    }
}
