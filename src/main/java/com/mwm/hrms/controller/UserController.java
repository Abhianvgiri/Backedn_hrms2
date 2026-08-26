package com.mwm.hrms.controller;

import com.mwm.hrms.entity.User;
import com.mwm.hrms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private String generateUniquePassword(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "user";
        }

        String firstName = fullName.trim().split("\\s+")[0].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (firstName.length() > 6) {
            firstName = firstName.substring(0, 6);
        }

        int randomNum = new Random().nextInt(900) + 100;

        return firstName + "mwm@" + randomNum;
    }

    @GetMapping("/sample-csv")
    public ResponseEntity<byte[]> downloadSampleCsv() {
        String csvHeader = "employee_code,full_name,email,role,phone,department,designation,base_salary,payment_mode,shift_timing,bank_name,account_no,ifsc_code\n";
        String sampleRow = "MWM-101,Rahul Sharma,rahul@mwm.com,EMPLOYEE,9876543210,dep-eng,Developer,50000,BANK,shift1,HDFC BANK,1234567890,HDFC0001234\n";

        byte[] bytes = (csvHeader + sampleRow).getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "employee_sample_template.csv");

        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @PostMapping("/import-csv")
    public ResponseEntity<byte[]> importEmployeesCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a valid CSV file!".getBytes());
        }

        List<String[]> credentialRecords = new ArrayList<>();
        credentialRecords.add(new String[]{"Employee Code", "Full Name", "Email", "Generated Password", "Role"});

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;

            List<User> usersToSave = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length < 8) continue;

                String empCode = data[0].trim();
                String fullName = data[1].trim();
                String email = data[2].trim();
                String role = data[3].trim().toUpperCase();

                String customPassword = generateUniquePassword(fullName);

                User user = new User();
                user.setEmployeeCode(empCode);
                user.setFullName(fullName);
                user.setEmail(email);
                user.setPassword(customPassword);
                user.setRole(role);
                user.setPhone(data.length > 4 ? data[4].trim() : "");
                user.setDepartment(data.length > 5 ? data[5].trim() : "dep-eng");
                user.setDesignation(data.length > 6 ? data[6].trim() : "Staff");

                try {
                    user.setBaseSalary(Double.parseDouble(data[7].trim()));
                    user.setFixedGrossSalary(Double.parseDouble(data[7].trim()));
                } catch (Exception e) {
                    user.setBaseSalary(30000.0);
                    user.setFixedGrossSalary(30000.0);
                }

                user.setPaymentMode(data.length > 8 ? data[8].trim() : "BANK");
                user.setShiftTiming(data.length > 9 && !data[9].trim().isEmpty() ? data[9].trim() : "shift1");
                user.setBankName(data.length > 10 ? data[10].trim() : "");
                user.setAccountNo(data.length > 11 ? data[11].trim() : "");
                user.setIfscCode(data.length > 12 ? data[12].trim() : "");

                user.setStatus("active");
                user.setJoiningDate(LocalDate.now());

                usersToSave.add(user);

                credentialRecords.add(new String[]{empCode, fullName, email, customPassword, role});
            }

            userRepository.saveAll(usersToSave);

            StringBuilder csvOutput = new StringBuilder();
            for (String[] row : credentialRecords) {
                csvOutput.append(String.join(",", row)).append("\n");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "employee_credentials_report.csv");

            return ResponseEntity.ok().headers(headers).body(csvOutput.toString().getBytes());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(("Error: " + e.getMessage()).getBytes());
        }
    }
    @GetMapping("/{id}/leave-balances")
    public ResponseEntity<?> getLeaveBalances(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        User user = userOpt.get();
        Map<String, Double> balances = new HashMap<>();
        balances.put("clBalance", user.getClBalance() != null ? user.getClBalance() : 0.0);
        balances.put("slBalance", user.getSlBalance() != null ? user.getSlBalance() : 0.0);
        balances.put("plBalance", user.getPlBalance() != null ? user.getPlBalance() : 0.0);
        balances.put("lwpCount", user.getLwpCount() != null ? user.getLwpCount() : 0.0);

        return ResponseEntity.ok(balances);
    }

    @PutMapping("/{id}/leave-balances")
    public ResponseEntity<?> updateLeaveBalances(@PathVariable Long id, @RequestBody Map<String, Double> payload) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        User user = userOpt.get();
        user.setClBalance(payload.getOrDefault("clBalance", user.getClBalance()));
        user.setSlBalance(payload.getOrDefault("slBalance", user.getSlBalance()));
        user.setPlBalance(payload.getOrDefault("plBalance", user.getPlBalance()));
        user.setLwpCount(payload.getOrDefault("lwpCount", user.getLwpCount()));

        userRepository.save(user);
        return ResponseEntity.ok("Leave balances updated successfully!");
    }


}