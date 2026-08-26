package com.mwm.hrms.controller;

import com.mwm.hrms.entity.HrPolicy;
import com.mwm.hrms.repository.HrPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/policies")
@CrossOrigin(origins = "*")
public class HrPolicyController {

    @Autowired
    private HrPolicyRepository policyRepository;

    public static String UPLOAD_DIR = "uploads/policies/";

    @GetMapping
    public ResponseEntity<List<HrPolicy>> getAllPolicies() {
        return ResponseEntity.ok(policyRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createPolicy(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "externalLink", required = false) String externalLink,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        try {
            HrPolicy policy = new HrPolicy();
            policy.setTitle(title);
            policy.setDescription(description);
            policy.setExternalLink(externalLink);
            policy.setCreatedAt(LocalDateTime.now());
            
            if (file != null && !file.isEmpty()) {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename().replace(" ", "_");
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                policy.setDocumentUrl("/uploads/policies/" + fileName);
            }

            HrPolicy saved = policyRepository.save(policy);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create policy: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePolicy(@PathVariable Long id) {
        if (!policyRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Policy not found");
        }
        policyRepository.deleteById(id);
        return ResponseEntity.ok("Policy deleted successfully");
    }
}
