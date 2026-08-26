package com.mwm.hrms.controller;

import com.mwm.hrms.entity.User;
import com.mwm.hrms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<User>> getAllEmployees() {
        List<User> employees = userRepository.findAll().stream()
                .filter(u -> "EMPLOYEE".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(employees);
    }

    @PostMapping
    public ResponseEntity<?> addEmployee(@RequestBody User employee) {
        if (userRepository.findByEmail(employee.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists!");
        }

        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        employee.setPassword(rawPassword);
        employee.setRole("EMPLOYEE");

        employee.setEmployeeCode("EMP-" + System.currentTimeMillis() % 100000);

        User savedUser = userRepository.save(employee);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Employee Created!");
        response.put("email", savedUser.getEmail());
        response.put("password", rawPassword);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getEmployeeProfile(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        }
        return ResponseEntity.badRequest().body("Profile not found");
    }
}