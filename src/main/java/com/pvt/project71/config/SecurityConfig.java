package com.pvt.project71.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.pvt.project71.handlers.OAuthSuccessHandler;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /*
    oauth2 default redirekt = /login
    google auth2 = /login/oauth2/code/google
    TODO: fixa oauth för andra organisationer, typ ladok/Antagning.se ?

     */
    private final UserService userService;
    private final RsaKeyProperties rsaKeyProperties;
    public SecurityConfig(UserService userService, RsaKeyProperties rsaKeyProperties){
        this.userService = userService;
        this.rsaKeyProperties = rsaKeyProperties;
    }


// Might be useful for further http oauth testing
    /**
     *
     *   @Bean
     *   * {
     *         return new InMemoryUserDetailsManager(
     *                 User.withUsername("jo")
     *                         .password("{noop}pass")
     *                         .authorities("read")
     *                         .build()
     *         );
     *     }
     * @return
     */
    @Bean
    JwtDecoder jwtDecoder() {

        return NimbusJwtDecoder.withPublicKey(rsaKeyProperties.publicKey()).build();
    }
    @Bean
    JwtEncoder jwtEncoder() {
        //Encryption, thank god for tutorials
        JWK jwk = new RSAKey.Builder(rsaKeyProperties.publicKey()).privateKey(rsaKeyProperties.privateKey()).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/test", "/", "/login").permitAll()
                        .anyRequest().permitAll() //Tempory access for now
                )
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .oauth2Login(oauth -> oauth
                        .successHandler(new OAuthSuccessHandler(jwtService))

                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }


}
