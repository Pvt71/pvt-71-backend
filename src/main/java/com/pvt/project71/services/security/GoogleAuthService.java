package com.pvt.project71.services.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public interface GoogleAuthService{
    GoogleIdToken verifyToken(String token);


}
