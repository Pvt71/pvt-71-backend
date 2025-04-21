package com.pvt.project71.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    @GetMapping(path = "/htest")
    public String htest(){
        return "hell";
    }
}
