package com.coursegen.coursegen_backend.controller;

import com.coursegen.coursegen_backend.Util.JwtUtil;
import com.coursegen.coursegen_backend.model.LoginRequest;
import com.coursegen.coursegen_backend.model.LoginResponse;
import com.coursegen.coursegen_backend.model.SignupRequest;
import com.coursegen.coursegen_backend.model.User;
import com.coursegen.coursegen_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        System.out.println("long1"+","+request.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        User user = userOpt.get();
        if (!BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(new LoginResponse(token, user.getEmail(), user.getName(), user.getId()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
       System.out.println("Yes");
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already in use");
        }

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setName(request.getName());
        newUser.setPasswordHash(hashedPassword);

        User user1=userRepository.save(newUser);

        String token = jwtUtil.generateToken(newUser.getEmail());
        return ResponseEntity.ok(new LoginResponse(token, newUser.getEmail(), newUser.getName(),user1.getId()));
    }
}
