package com.dhn.client.dao;

import com.dhn.client.bean.Msg_Log;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("cmsDao")
public class CmsRequestDAOImpl extends AbstractRequestDAO {

    @Override
    protected String getNamespace() {
        return "com.dhn.client.cms.mapper.SendRequest";
    }

    @Override
    public void doUpdateInvalidDataTx(List<String> invalidList, Msg_Log ml) {
        Map<String, Object> param = new HashMap<>();
        param.put("list", invalidList);
        param.put("ml", ml);

        sqlSession.update(getNamespace() + ".invalid_update", param);
        sqlSession.insert(getNamespace() + ".invalid_log_insert", param);
        sqlSession.delete(getNamespace() + ".invalid_delete", param);

    }
}