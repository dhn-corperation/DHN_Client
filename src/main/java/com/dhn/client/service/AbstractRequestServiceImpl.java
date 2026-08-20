package com.dhn.client.service;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import com.dhn.client.dao.AbstractRequestDAO;
import java.util.List;

public abstract class AbstractRequestServiceImpl implements RequestService {

    protected abstract AbstractRequestDAO getDao();

    @Override
    // ⭐️ 해결: RequestBean_bk 에서 RequestBean 으로 수정 완료!
    public List<RequestBean> selectRequests(SQLParameter param) throws Exception {
        return getDao().selectRequests(param);
    }

    @Override
    public void updateSendComplete(SQLParameter param) throws Exception {
        getDao().updateSendComplete(param);
    }

    @Override
    public void updateInvalidData(List<String> invalidList, Msg_Log ml) throws Exception {
        getDao().updateInvalidData(invalidList, ml);
    }

    @Override
    public void applyResultProcess(Msg_Log ml) throws Exception {
        getDao().applyResultProcess(ml);
    }
}