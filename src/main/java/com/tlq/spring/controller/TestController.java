package com.tlq.spring.controller;

import com.tlq.spring.service.TestService;
import com.tlq.spring.simple.annotation.Autowired;
import com.tlq.spring.simple.annotation.Controller;
import com.tlq.spring.simple.annotation.RequestMapping;
import com.tlq.spring.simple.annotation.RequestParam;

/**
 * @Description: 测试 Controller
 * @Author: TanLinquan
 * @Date: 2021/1/10 20:15
 * @Version: V1.0
 **/
@Controller
public class TestController {

    @Autowired
    private TestService testService;

    @RequestMapping(value = "/test")
    public String test(@RequestParam("name") String name) {
        return testService.test(name);
    }
}
