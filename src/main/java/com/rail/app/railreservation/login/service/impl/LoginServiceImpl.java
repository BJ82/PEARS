package com.rail.app.railreservation.login.service.impl;

import com.rail.app.railreservation.login.dto.LoginRequest;
import com.rail.app.railreservation.login.dto.LoginResponse;
import com.rail.app.railreservation.login.service.LoginService;
import com.rail.app.railreservation.security.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    private AuthenticationManager authenticationManager;

    private JwtUtil jwtUtil;

    public LoginServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest loginRequest) throws AuthenticationException {

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));

        return new LoginResponse(jwtUtil.generateToken(username),"Login Success");
    }
}
