package com.rail.app.railreservation.signup.service.impl;

import com.rail.app.railreservation.security.entity.Users;
import com.rail.app.railreservation.security.repository.UserDetailsRepository;
import com.rail.app.railreservation.security.role.Role;
import com.rail.app.railreservation.signup.dto.SignUpRequest;
import com.rail.app.railreservation.signup.dto.SignUpResponse;
import com.rail.app.railreservation.signup.exception.UserPresentException;
import com.rail.app.railreservation.signup.service.SignUpService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignUpServiceImpl implements SignUpService {

    private UserDetailsRepository userDetailsRepo;

    private ModelMapper mapper;

    private PasswordEncoder passwordEncoder;

    public SignUpServiceImpl(ModelMapper mapper, UserDetailsRepository userDetailsRepo,PasswordEncoder passwordEncoder) {
        this.mapper = mapper;
        this.userDetailsRepo = userDetailsRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public SignUpResponse signUpUser(SignUpRequest signUpRequest, Role role){

        String username = signUpRequest.getUsername();

        if(isUserPresent(username))
            throw new UserPresentException("UserName:"+username+" Already Registered");

        Users users = new Users();
        users.setUsername(username);
        users.setRole(role);
        users.setEmail(signUpRequest.getEmail());

        String pswrdEncoded = passwordEncoder.encode(signUpRequest.getPassword());
        users.setPassword(pswrdEncoded);

        users = userDetailsRepo.save(users);

        SignUpResponse signUpResponse = null;

        if(users.getId() !=0 ){
            signUpResponse = mapper.map(signUpRequest, SignUpResponse.class);
            signUpResponse.setRegistered(true);
        }
        return signUpResponse;
    }

    private boolean isUserPresent(String username){

        return userDetailsRepo.findByUsername(username).isPresent();
    }
}
