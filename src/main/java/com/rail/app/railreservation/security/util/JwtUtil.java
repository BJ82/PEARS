package com.rail.app.railreservation.security.util;

import com.rail.app.railreservation.security.service.UserDetailsServiceImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {


    private final String SECRET;
    private final SecretKey key;

    private final long EXPIRATION_TIME;

    private final UserDetailsServiceImpl userService;

    public JwtUtil(@Value("${jwt.secret.key}") String SECRET,
                   @Value("${jwt.expiry.time}") String EXPIRATION_TIME,
                   UserDetailsServiceImpl userService) {

        this.userService = userService;
        this.SECRET = SECRET;
        this.key = Keys.hmacShaKeyFor(this.SECRET.getBytes());
        this.EXPIRATION_TIME = Long.parseLong(EXPIRATION_TIME);
    }

    public String generateToken(String username){

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isJWTValid(String jwt){

        boolean isJWTValid = true;

        if(isNonEmpty(jwt) && isNotNull(jwt)) {

            JwtParser jwtParser = Jwts.parserBuilder()
                                .setSigningKey(key)
                                .build();

            Jws jwtToken = (Jws) jwtParser.parse(jwt);
            Claims bodyJwt = (Claims) jwtToken.getBody();


            //Check username against DB
            String userNameFrmJwt = bodyJwt.getSubject();
            UserDetails userDetails = userService.loadUserByUsername(userNameFrmJwt);

            if(userDetails !=null && isNotNull(userDetails.getUsername())){
                if (!userDetails.getUsername().equals(userNameFrmJwt))
                    isJWTValid = false;
            }


            //Check expiration
            Date expiryDate = bodyJwt.getExpiration();
            if(expiryDate != null){
                if (!expiryDate.after(new Date()))
                    isJWTValid = false;
            }
        }
        return isJWTValid;
    }

    public String getUserName(String token){

        JwtParser jwtParser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();

        Jws jwtToken = (Jws) jwtParser.parse(token);
        Claims bodyJwt = (Claims) jwtToken.getBody();

        return bodyJwt.getSubject();
    }

    private boolean isNonEmpty(String str){

        boolean isNonEmpty = true;
        isNonEmpty = !("".equals(str));
        return isNonEmpty;
    }

    private boolean isNotNull(String str){

        return (str == null)?true:false;

    }

}

