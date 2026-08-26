package com.mwm.hrms.controller;

import com.mwm.hrms.entity.OfficialDocument;
import com.mwm.hrms.entity.User;
import com.mwm.hrms.repository.DocumentRepository;
import com.mwm.hrms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    public static String UPLOAD_DIR = "uploads/documents/";

    @GetMapping
    public ResponseEntity<?> getDocuments(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        User user = userOpt.get();
        boolean isHr = "HR".equalsIgnoreCase(user.getRole()) || "HOD".equalsIgnoreCase(user.getRole());

        List<OfficialDocument> docs = isHr ? documentRepository.findAllByOrderByIssuedAtDesc() : documentRepository.findByUserIdOrderByIssuedAtDesc(user.getId());

        List<Map<String, Object>> response = new ArrayList<>();
        for (OfficialDocument d : docs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("employeeId", d.getUserId());
            Optional<User> emp = userRepository.findById(d.getUserId());
            map.put("employeeName", emp.isPresent() ? emp.get().getFullName() : "Unknown");
            map.put("title", d.getTitle());
            map.put("type", d.getType());
            map.put("fileUrl", d.getFileUrl());
            map.put("issuedAt", d.getIssuedAt().toString());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("title") String title,
            @RequestParam("type") String type) {

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename().replace(" ", "_");
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            OfficialDocument doc = new OfficialDocument();
            doc.setUserId(userId);
            doc.setTitle(title);
            doc.setType(type);
            doc.setFileUrl("/uploads/documents/" + fileName);
            doc.setIssuedAt(LocalDate.now());

            documentRepository.save(doc);

            return ResponseEntity.ok("Document uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<Map<String, Object>> response = new ArrayList<>();
        for (User u : userRepository.findAll()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getFullName());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }
}