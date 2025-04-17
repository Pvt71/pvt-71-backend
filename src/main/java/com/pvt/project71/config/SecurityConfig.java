package com.pvt.project71.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("Custom security config loaded");
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2Login(AbstractHttpConfigurer::disable)//Ska vara enabled senare
                .httpBasic(AbstractHttpConfigurer::disable)//Ska vara disabled
                .formLogin(AbstractHttpConfigurer::disable)//ska vara disabled
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .build();
    }
}
