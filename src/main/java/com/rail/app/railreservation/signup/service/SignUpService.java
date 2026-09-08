package com.rail.app.railreservation.signup.service;

import com.rail.app.railreservation.security.entity.Users;
import com.rail.app.railreservation.security.role.Role;
import com.rail.app.railreservation.signup.dto.SignUpRequest;
import com.rail.app.railreservation.signup.dto.SignUpResponse;
import com.rail.app.railreservation.signup.exception.UserPresentException;

public interface SignUpService {

    public SignUpResponse signUpUser(SignUpRequest signUpRequest, Role role);

}
