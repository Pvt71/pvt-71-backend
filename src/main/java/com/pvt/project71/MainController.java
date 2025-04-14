package com.pvt.project71;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path = "/test")
public class MainController {

    @GetMapping(value = "/hello")
    public @ResponseBody String hello() {
        return "Hello World! //Project 71"; // This is a test message
    }
}
