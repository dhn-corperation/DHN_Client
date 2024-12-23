package com.dhn.client.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TemplateReqImpl implements TemplateReqDAO{

    @Autowired
    private SqlSession sqlSession;
}
