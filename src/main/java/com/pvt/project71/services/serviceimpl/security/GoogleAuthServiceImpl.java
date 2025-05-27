package com.pvt.project71.services.serviceimpl.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.pvt.project71.services.security.GoogleAuthService;
import com.pvt.project71.services.security.GoogleAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {
    //private static final Logger logger = LoggerFactory.getLogger(GoogleAuthServiceImpl.class);

    private final   HttpTransport transport;
    private  final GoogleIdTokenVerifier verifer;

    @Value("${backend.secret}")
    private String backendSecret;

    @Value("${frontend.secret}")
    private String frontendSecret;

    public GoogleAuthServiceImpl() throws GeneralSecurityException, IOException {
        this.transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        this.verifer = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Arrays.asList(frontendSecret, backendSecret))
                .setAcceptableTimeSkewSeconds(60)
                .build();

    }


    @Override
        public GoogleIdToken verifyToken(String token) {
        try {
         /*
             try {
                String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
                HttpResponse response = transport.createRequestFactory()
                        .buildGetRequest(new GenericUrl(url))
                        .execute();
                logger.info("Google verification response: {}", response.parseAsString());
            } catch (IOException e) {
                logger.error("Debug failed", e);
            }
            logger.debug("Attempting to verify " + token);
            logger.debug("token is" + verifer.verify(token));
          */
            return verifer.verify(token);

        } catch (Exception e) {
            return null;
        }
    }


}