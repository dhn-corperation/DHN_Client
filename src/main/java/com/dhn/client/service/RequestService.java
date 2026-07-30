package com.dhn.client.service;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import java.util.List;

public interface RequestService {
    int selectRequestCount(SQLParameter param) throws Exception;
    void updateGroupNo(SQLParameter param) throws Exception;

    // ⭐️ 리턴 타입 RequestBean 으로 깔끔하게 통일!
    List<RequestBean> selectRequests(SQLParameter param) throws Exception;

    void updateSendComplete(SQLParameter param) throws Exception;
    void updateSendInit(SQLParameter param) throws Exception;
    void updateInvalidData(List<String> invalidList, Msg_Log ml) throws Exception;
}