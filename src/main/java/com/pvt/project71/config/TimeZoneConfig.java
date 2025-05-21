package com.pvt.project71.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"));
        System.out.println("Time zone set to: " + TimeZone.getDefault().getID()); //För dubbelkolla att det funkar
    }
}