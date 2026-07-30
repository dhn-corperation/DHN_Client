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

    // ⭐️ 핵심: 부모의 메서드를 덮어써서 CMS 전용 로직으로 바꿈!
    @Override
    public void doUpdateInvalidDataTx(List<String> invalidList, Msg_Log ml) {
        Map<String, Object> param = new HashMap<>();
        param.put("list", invalidList);
        param.put("ml", ml);

        // 1. 발송 테이블의 상태값을 4(또는 9)와 에러코드 7999로 변경만 함
        sqlSession.update(getNamespace() + ".invalid_update", param);

        // 👉 로그 테이블이 없으므로 INSERT 와 DELETE 구문은 과감히 삭제!
    }
}