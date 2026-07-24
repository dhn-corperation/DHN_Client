package com.dhn.client.service;

import com.dhn.client.dao.BMRequestDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BMRequestServiceimpl implements BMRequestService {

    @Autowired
    private BMRequestDAO bmRequestDAO;


}
