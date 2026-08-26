package com.mwm.hrms.controller;

import com.mwm.hrms.entity.LeaveRequest;
import com.mwm.hrms.entity.User;
import com.mwm.hrms.repository.LeaveRepository;
import com.mwm.hrms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "*")
public class LeaveController {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private UserRepository userRepository;

    public static class LeaveDto {
        public String email;
        public String type;
        public String dayType;
        public String fromDate;
        public String toDate;
        public double days;
        public String reason;
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyLeave(@RequestBody LeaveDto request) {
        Optional<User> userOpt = userRepository.findByEmail(request.email);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found!");

        LeaveRequest leave = new LeaveRequest();
        leave.setUser(userOpt.get());

        leave.setLeaveType(request.type.toUpperCase());

        leave.setDayType(request.dayType);
        leave.setFromDate(LocalDate.parse(request.fromDate));
        leave.setToDate(LocalDate.parse(request.toDate));
        leave.setDays(request.days);
        leave.setReason(request.reason);
        leave.setStatus("PENDING");

        leaveRepository.save(leave);
        return ResponseEntity.ok("Leave request submitted successfully!");
    }

    @GetMapping("/my-leaves")
    public ResponseEntity<?> getMyLeaves(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found!");

        List<LeaveRequest> leaves = leaveRepository.findByUserIdOrderByAppliedOnDesc(userOpt.get().getId());

        List<Map<String, Object>> response = new ArrayList<>();
        for (LeaveRequest l : leaves) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", l.getId());
            map.put("employeeId", l.getUser().getId());
            map.put("employeeName", l.getUser().getFullName());
            map.put("type", l.getLeaveType().toLowerCase());
            map.put("dayType", l.getDayType());
            map.put("fromDate", l.getFromDate().toString());
            map.put("toDate", l.getToDate().toString());
            map.put("days", l.getDays());
            map.put("reason", l.getReason());
            map.put("status", l.getStatus().toLowerCase());

            response.add(map);
        }

        return ResponseEntity.ok(response);
    }
    public static class LeaveStatusDto {
        public Long leaveId;
        public String status;
        public String actionByEmail;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllLeaves() {
        List<LeaveRequest> leaves = leaveRepository.findAllByOrderByAppliedOnDesc();

        List<Map<String, Object>> response = new ArrayList<>();
        for (LeaveRequest l : leaves) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", l.getId());
            map.put("employeeId", l.getUser().getId());
            map.put("employeeName", l.getUser().getFullName());
            map.put("type", l.getLeaveType().toLowerCase());
            map.put("dayType", l.getDayType());
            map.put("fromDate", l.getFromDate().toString());
            map.put("toDate", l.getToDate().toString());
            map.put("days", l.getDays());
            map.put("reason", l.getReason());
            map.put("status", l.getStatus().toLowerCase());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-status")
    public ResponseEntity<?> updateLeaveStatus(@RequestBody LeaveStatusDto request) {
        Optional<LeaveRequest> leaveOpt = leaveRepository.findById(request.leaveId);
        if (leaveOpt.isEmpty()) return ResponseEntity.badRequest().body("Leave request not found!");

        Optional<User> actionByOpt = userRepository.findByEmail(request.actionByEmail);
        if (actionByOpt.isEmpty()) return ResponseEntity.badRequest().body("HR/HOD not found!");

        LeaveRequest leave = leaveOpt.get();
        leave.setStatus(request.status.toUpperCase());
        leave.setApprovedBy(actionByOpt.get().getId());

        leaveRepository.save(leave);

        if ("APPROVED".equalsIgnoreCase(request.status)) {
            User emp = leave.getUser();
            double days = leave.getDays();
            String type = leave.getLeaveType().toLowerCase();

            if (type.contains("casual") || type.equals("cl")) {
                double currentBal = emp.getClBalance() != null ? emp.getClBalance() : 12.0;
                emp.setClBalance(Math.max(0, currentBal - days));
            } else if (type.contains("sick") || type.equals("sl")) {
                double currentBal = emp.getSlBalance() != null ? emp.getSlBalance() : 12.0;
                emp.setSlBalance(Math.max(0, currentBal - days));
            } else if (type.contains("earned") || type.equals("pl") || type.contains("wfh")) {
                double currentBal = emp.getPlBalance() != null ? emp.getPlBalance() : 15.0;
                emp.setPlBalance(Math.max(0, currentBal - days));
            } else if (type.contains("lwp")) {
                double currentLwp = emp.getLwpCount() != null ? emp.getLwpCount() : 0.0;
                emp.setLwpCount(currentLwp + days);
            }
            userRepository.save(emp);
        }

        return ResponseEntity.ok("Leave " + request.status.toLowerCase() + " successfully!");
    }

    // ==========================================
    // LEAVE BALANCE APIs (HR ONLY)
    // ==========================================

    @GetMapping("/{id}/leave-balances")
    public ResponseEntity<?> getLeaveBalances(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        User user = userOpt.get();
        Map<String, Double> balances = new HashMap<>();

        // ✅ FIX: 0.0 ki jagah default (12, 12, 15) set kar diya gaya hai
        balances.put("clBalance", user.getClBalance() != null ? user.getClBalance() : 12.0);
        balances.put("slBalance", user.getSlBalance() != null ? user.getSlBalance() : 12.0);
        balances.put("plBalance", user.getPlBalance() != null ? user.getPlBalance() : 15.0);
        balances.put("lwpCount", user.getLwpCount() != null ? user.getLwpCount() : 0.0);

        return ResponseEntity.ok(balances);
    }

    @PutMapping("/balances/{userId}")
    public ResponseEntity<?> updateLeaveBalances(@PathVariable Long userId, @RequestBody Map<String, Double> payload) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        User user = userOpt.get();
        user.setClBalance(payload.getOrDefault("clBalance", user.getClBalance()));
        user.setSlBalance(payload.getOrDefault("slBalance", user.getSlBalance()));
        user.setPlBalance(payload.getOrDefault("plBalance", user.getPlBalance()));
        user.setLwpCount(payload.getOrDefault("lwpCount", user.getLwpCount()));

        userRepository.save(user);
        Map<String, String> res = new HashMap<>();
        res.put("message", "Leave balances updated successfully!");
        return ResponseEntity.ok(res);
    }
}