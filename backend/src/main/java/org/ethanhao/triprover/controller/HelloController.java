package org.ethanhao.triprover.controller;

import org.ethanhao.triprover.domain.ResponseResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @RequestMapping("/hello")
    @PreAuthorize("hasAuthority('user:all')")
    public String hello() {
        return "Hello Spring Boot!";
    }

    @PostMapping("/testCors")
    public ResponseResult test(){
        System.out.println("testCors");
        return new ResponseResult(200,"testCors");
    }
}
