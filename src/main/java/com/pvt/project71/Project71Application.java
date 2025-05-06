package com.pvt.project71;

import com.pvt.project71.config.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import org.springframework.web.bind.annotation.CrossOrigin;

@EnableConfigurationProperties(RsaKeyProperties.class)
@SpringBootApplication
@CrossOrigin
public class Project71Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Project71Application.class, args);
	}

}
