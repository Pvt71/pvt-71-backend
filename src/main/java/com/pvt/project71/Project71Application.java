package com.pvt.project71;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication(scanBasePackages = "com.pvt")
@CrossOrigin
public class Project71Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Project71Application.class, args);
	}

}
