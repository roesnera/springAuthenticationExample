package com.example.authenticationpractice2.auth;

import com.example.authenticationpractice2.config.JwtService;
import com.example.authenticationpractice2.user.Role;
import com.example.authenticationpractice2.user.User;
import com.example.authenticationpractice2.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final JwtService service;
    private final AuthenticationManager manager;
    private final PasswordEncoder passwordEncoder;

    // builds a new user and enters them into the db with the role of User
    // if we wanted to introduce some kind of qualifications for creating a new user, we would do so here
    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        System.out.println(request.getEmail());
        if(repository.findByEmail(request.getEmail()).isEmpty()){
            repository.save(user);
        }
        var jwtToken = service.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    // authenticates the user based on request credentials against the existing user database
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        manager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = repository.findByEmail(request.getEmail()).orElseThrow();

        var jwtToken = service.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }
}
