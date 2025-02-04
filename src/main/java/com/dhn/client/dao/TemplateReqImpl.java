package com.dhn.client.dao;

import com.dhn.client.bean.ButtonBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.bean.TmplData;
import com.dhn.client.bean.TmplRequestBean;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class TemplateReqImpl implements TemplateReqDAO{

    @Autowired
    private SqlSession sqlSession;

    @Override
    public int selectTmplRequestCount(SQLParameter param) throws Exception {
        int cnt = 0;
        cnt = sqlSession.selectOne("com.dhn.client.tmpl.mapper.SendRequest.req_tmpl_count",param);
        return cnt;
    }

    @Override
    public List<TmplData> selectTmplData(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.tmpl.mapper.SendRequest.req_tmpl_data",param);
    }

    @Override
    public List<ButtonBean> selectBtnList(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.tmpl.mapper.SendRequest.req_btn_list",param);
    }

    @Override
    public void updateTmplfail(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.tmpl.mapper.SendRequest.update_tmpl_fail",param);
    }

    @Override
    public void updateTmplSuccess(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.tmpl.mapper.SendRequest.update_tmpl_success",param);
    }

    @Override
    public int selectInsRequestCount(SQLParameter param) throws Exception {
        return sqlSession.selectOne("com.dhn.client.tmpl.mapper.SendRequest.req_ins_count",param);
    }

    @Override
    public List<TmplData> selectTmplInsData(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.tmpl.mapper.SendRequest.req_ins_list",param);
    }

    @Override
    public void updateTmplInsAPR(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.tmpl.mapper.SendRequest.update_tmpl_apr",param);
    }

    @Override
    public void updateTmplInsREJ(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.tmpl.mapper.SendRequest.update_tmpl_rej",param);
    }

    @Override
    public int selectRefreshTmplCount(SQLParameter param) throws Exception {
        return sqlSession.selectOne("com.dhn.client.tmpl.mapper.SendRequest.req_refresh_count",param);
    }

    @Override
    public List<TmplData> selectTmplRefreshData(SQLParameter param) throws Exception {
        return sqlSession.selectList("com.dhn.client.tmpl.mapper.SendRequest.req_refresh_list",param);
    }

    @Override
    public void updateTmplRefresh(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.tmpl.mapper.SendRequest.update_tmpl_refresh",param);
    }

    @Override
    public void updateTmplrefreshfail(SQLParameter param) throws Exception {
        sqlSession.update("com.dhn.client.tmpl.mapper.SendRequest.update_tmpl_refresh_fail",param);
    }

    @Override
    public void selectInsertComments(SQLParameter param) throws Exception {

        int cnt = 0;
        cnt = sqlSession.selectOne("com.dhn.client.tmpl.mapper.SendRequest.select_comment_count",param);

        if (cnt < 1) {
            sqlSession.insert("com.dhn.client.tmpl.mapper.SendRequest.insert_comments",param);
        }
    }
}
