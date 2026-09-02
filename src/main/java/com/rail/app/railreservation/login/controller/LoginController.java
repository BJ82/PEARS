package com.rail.app.railreservation.login.controller;

import com.rail.app.railreservation.login.dto.LoginRequest;
import com.rail.app.railreservation.login.dto.LoginResponse;
import com.rail.app.railreservation.login.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/")
public class LoginController {

   private LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    //Ideally should be idempotent.
    //Use put or patch
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){

        return ResponseEntity.ok().body(loginService.login(loginRequest));
    }

}
