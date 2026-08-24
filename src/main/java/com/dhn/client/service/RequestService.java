package com.dhn.client.service;

import com.dhn.client.bean.Msg_Log;
import com.dhn.client.bean.RequestBean;
import com.dhn.client.bean.SQLParameter;
import java.util.List;

public interface RequestService {
    List<RequestBean> selectRequests(SQLParameter param) throws Exception;
    void updateSendComplete(SQLParameter param) throws Exception;
    void updateInvalidData(List<String> invalidList, Msg_Log ml) throws Exception;

    // 결과 처리 프로세스 (Update -> Insert -> Delete)
    void applyResultProcess(Msg_Log ml) throws Exception;
}