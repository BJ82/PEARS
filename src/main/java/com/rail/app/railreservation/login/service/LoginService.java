package com.rail.app.railreservation.login.service;

import com.rail.app.railreservation.login.dto.LoginRequest;
import com.rail.app.railreservation.login.dto.LoginResponse;
import org.springframework.security.core.AuthenticationException;

public interface LoginService {

    public LoginResponse login(LoginRequest loginRequest) throws AuthenticationException;

}
