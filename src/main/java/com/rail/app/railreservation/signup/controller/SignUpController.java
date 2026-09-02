package com.rail.app.railreservation.signup.controller;

import com.rail.app.railreservation.security.role.Role;
import com.rail.app.railreservation.signup.dto.SignUpRequest;
import com.rail.app.railreservation.signup.dto.SignUpResponse;
import com.rail.app.railreservation.signup.service.SignUpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/")
public class SignUpController {

    private SignUpService signUpService;

    public SignUpController(SignUpService signUpService) {
        this.signUpService = signUpService;
    }

    //Ideally should be idempotent.
    //Use put or patch
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("admin")
    public ResponseEntity<SignUpResponse> signUpAdmin(@RequestBody SignUpRequest signUpRequest){

        SignUpResponse signUpResponse;
        signUpResponse = signUpService.signUpUser(signUpRequest, Role.ADMIN);

        return ResponseEntity.status(HttpStatus.CREATED).body(signUpResponse);
    }

    //Ideally should be idempotent.
    //Use put or patch

    @PostMapping("user")
    public ResponseEntity<SignUpResponse> signUpUser(@RequestBody SignUpRequest signUpRequest){

        SignUpResponse signUpResponse;
        signUpResponse = signUpService.signUpUser(signUpRequest, Role.USER);

        return ResponseEntity.status(HttpStatus.CREATED).body(signUpResponse);
    }
}
