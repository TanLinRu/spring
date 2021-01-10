package com.tlq.spring.service.impl;

import com.tlq.spring.service.TestService;
import com.tlq.spring.simple.annotation.Service;

/**
 * @Description:
 * @Author: TanLinquan
 * @Date: 2021/1/10 20:14
 * @Version: V1.0
 **/
@Service
public class TestServiceImpl implements TestService {
    @Override
    public String test(String name) {
        return name;
    }
}
