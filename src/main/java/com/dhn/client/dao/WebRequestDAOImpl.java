package com.dhn.client.dao;

import com.dhn.client.bean.Msg_Log;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("webDao")
public class WebRequestDAOImpl extends AbstractRequestDAO {

    @Override
    protected String getNamespace() {
        // ⭐️ web.xml 에 정의된 namespace 와 정확히 일치시켜 줍니다.
        return "com.dhn.client.web.mapper.SendRequest";
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
