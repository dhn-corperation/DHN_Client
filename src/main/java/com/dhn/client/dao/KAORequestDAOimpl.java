package com.dhn.client.dao;

import com.dhn.client.bean.KAORequestBean;
import com.dhn.client.bean.SQLParameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class KAORequestDAOimpl implements KAORequestDAO{

    @Autowired
    private SqlSession sqlSession;

    @Override
    public int selectKAORequestCount(SQLParameter param) throws Exception {
        int cnt = 0;
        cnt = sqlSession.selectOne("com.dhn.client.kakao.mapper.SendRequest.at_count",param);
        return cnt;
    }

    @Override
    public void updateKAOGroupNo(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.at_group_update",param);
    }

    @Override
    public List<KAORequestBean> selectKAORequests(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.kakao.mapper.SendRequest.at_select", param);
    }

    @Override
    public void updateKAOSendComplete(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.at_sent_complete", param);
    }

    @Override
    public void updateKAOSendInit(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.kakao.mapper.SendRequest.at_sent_init", param);
    }

}
