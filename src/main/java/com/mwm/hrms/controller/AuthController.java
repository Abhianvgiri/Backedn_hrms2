package com.mwm.hrms.controller;

import com.mwm.hrms.dto.AuthResponse;
import com.mwm.hrms.dto.LoginRequest;
import com.mwm.hrms.entity.User;
import com.mwm.hrms.repository.UserRepository;
import com.mwm.hrms.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(request.getPassword())) {
                String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

                return ResponseEntity.ok(new AuthResponse(
                        token, user.getEmail(), user.getRole(), user.getFullName(), user.getEmployeeCode()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Password!");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found!");
    }
}