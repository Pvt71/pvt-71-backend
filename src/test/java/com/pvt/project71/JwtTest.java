package com.pvt.project71;


import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.services.JwtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.temporal.ChronoUnit;
import java.util.Base64;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JwtTest {

    private final JwtService jwtService;
    private final JwtDecoder decoder;
    private static final String INVALID_SIGNATURE = "INVALID_TEST_SIGNATURE";

    @Autowired
    public JwtTest(JwtService jwtService, JwtDecoder decoder) {
        this.jwtService = jwtService;
        this.decoder = decoder;
    }
    @Test
    public void testMockOAuth2DoesNotThrow(){
        UserEntity user = TestDataUtil.createValidTestUserEntity();
        Jwt jwt = jwtService.mockOauth2(user,1, ChronoUnit.MINUTES);

        Assertions.assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                decoder.decode(jwt.getTokenValue());
            }
        });
    }
    @Test
    public void testExpireddMockAuth2ThrowsBadJwT(){
        UserEntity user = TestDataUtil.createValidTestUserEntity();
        //Ensure that token expires
        Jwt jwt = jwtService.mockOauth2(user,1, ChronoUnit.NANOS);
        Assertions.assertThrowsExactly(BadJwtException.class,() ->  decoder.decode(jwt.getTokenValue()));
    }
    @Test
    public void testInvalidJwtSignatureThrowsBadJwt(){
        //Valid jwt
        Jwt jwt = jwtService.mockOauth2(TestDataUtil.createValidTestUserEntity(),1,ChronoUnit.MINUTES);
        String[] values = jwt.getTokenValue().split("\\.");
        //Change signature to invalid one
        values[2] = Base64.getUrlEncoder().withoutPadding().encodeToString(INVALID_SIGNATURE.getBytes());
        //Create new Jwt with invalid signature
        Jwt invalidSignedJwt = new Jwt(values[0]+ "." + values[1] + "." + values[2],jwt.getIssuedAt(),jwt.getExpiresAt(),jwt.getHeaders(),jwt.getClaims());
        Assertions.assertThrowsExactly(BadJwtException.class,() -> decoder.decode(invalidSignedJwt.getTokenValue()));
    }
    @Test
    public void testMockOAuth2DecodedValueIsCorrect(){
        UserEntity user = TestDataUtil.createValidTestUserEntity();
        Jwt jwt = jwtService.mockOauth2(user,1, ChronoUnit.MINUTES);
       Assertions.assertEquals(user.getEmail(),decoder.decode(jwt.getTokenValue()).getSubject());
    }

}
