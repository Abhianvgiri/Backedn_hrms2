package com.mwm.hrms.controller;

import com.mwm.hrms.entity.AttendanceLog;
import com.mwm.hrms.entity.User;
import com.mwm.hrms.repository.AttendanceRepository;
import com.mwm.hrms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private static final double OFFICE_LAT = 28.433026;
    private static final double OFFICE_LNG = 77.037061;
    private static final double ALLOWED_RADIUS_METERS = 100.0;

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static class PunchRequest {
        public String email;
        public double latitude;
        public double longitude;
        public String address;
    }

    @PostMapping("/punch-in")
    public ResponseEntity<?> punchIn(@RequestBody PunchRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.email);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found!");

        User user = userOpt.get();
        LocalDate today = LocalDate.now();

        Optional<AttendanceLog> existingLog = attendanceRepository.findByUserIdAndAttendanceDate(user.getId(), today);
        if (existingLog.isPresent() && existingLog.get().getPunchIn() != null) {
            return ResponseEntity.badRequest().body("You have already punched in today!");
        }

        AttendanceLog log = existingLog.orElse(new AttendanceLog());
        log.setUserId(user.getId());
        log.setAttendanceDate(today);
        log.setPunchIn(LocalDateTime.now());

        String fullLocation = (request.address != null ? request.address : "Unknown Area") + " (Lat: " + request.latitude + ", Lng: " + request.longitude + ")";
        log.setInLocation(fullLocation);

        LocalDateTime shiftStartTime = today.atTime(9, 45);
        if (LocalDateTime.now().isAfter(shiftStartTime)) {
            log.setStatus("PENDING_APPROVAL");
        } else {
            log.setStatus("PRESENT");
        }

        attendanceRepository.save(log);
        
        if ("PENDING_APPROVAL".equals(log.getStatus())) {
            return ResponseEntity.ok("You are late! Punch-in request sent for approval.");
        }
        return ResponseEntity.ok("Successfully Punched In! Status: " + log.getStatus());
    }
    
    public static class ApprovalRequest {
        public Long logId;
        public String status;
    }

    @PostMapping("/approve-late")
    public ResponseEntity<?> approveLatePunch(@RequestBody ApprovalRequest request) {
        Optional<AttendanceLog> logOpt = attendanceRepository.findById(request.logId);
        if (logOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Attendance log not found!");
        }
        AttendanceLog log = logOpt.get();
        
        if ("LATE".equalsIgnoreCase(request.status) || "APPROVE".equalsIgnoreCase(request.status)) {
            if (log.getAttendanceDate().isBefore(LocalDate.now())) {
                log.setStatus("PRESENT");
            } else {
                log.setStatus("LATE");
            }
            attendanceRepository.save(log);
            return ResponseEntity.ok("Request approved and marked as " + log.getStatus());
        } else {
            log.setStatus("ABSENT");
            attendanceRepository.save(log);
            return ResponseEntity.ok("Request rejected and marked as ABSENT");
        }
    }

    @PostMapping("/punch-out")
    public ResponseEntity<?> punchOut(@RequestBody PunchRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.email);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found!");

        User user = userOpt.get();
        LocalDate today = LocalDate.now();

        Optional<AttendanceLog> existingLog = attendanceRepository.findByUserIdAndAttendanceDate(user.getId(), today);

        if (existingLog.isEmpty() || existingLog.get().getPunchIn() == null) {
            return ResponseEntity.badRequest().body("You haven't punched in today!");
        }

        AttendanceLog log = existingLog.get();

        if (log.getPunchOut() != null) {
            return ResponseEntity.badRequest().body("You have already punched out today!");
        }

        log.setPunchOut(LocalDateTime.now());

        String fullLocation = (request.address != null ? request.address : "Unknown Area") + " (Lat: " + request.latitude + ", Lng: " + request.longitude + ")";
        log.setOutLocation(fullLocation);

        long minutes = Duration.between(log.getPunchIn(), log.getPunchOut()).toMinutes();
        double hoursWorked = minutes / 60.0;
        log.setOtHours(hoursWorked);

        if (hoursWorked < 5.0 && !log.getStatus().equals("LATE")) {
            log.setStatus("HALF-DAY");
        }

        attendanceRepository.save(log);
        return ResponseEntity.ok("Duty Over! Total Work Time: " + (minutes / 60) + "h " + (minutes % 60) + "m");
    }

    public static class PastRequest {
        public String email;
        public String date;
        public String punchIn;
        public String punchOut;
        public String reason;
    }

    @PostMapping("/request-past")
    public ResponseEntity<?> requestPastAttendance(@RequestBody PastRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.email);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found!");

        User user = userOpt.get();
        LocalDate reqDate = LocalDate.parse(request.date);

        if (reqDate.isAfter(LocalDate.now()) || reqDate.isEqual(LocalDate.now())) {
            return ResponseEntity.badRequest().body("Past requests can only be made for previous dates.");
        }

        Optional<AttendanceLog> existingLog = attendanceRepository.findByUserIdAndAttendanceDate(user.getId(), reqDate);
        if (existingLog.isPresent() && ("PRESENT".equals(existingLog.get().getStatus()) || "HALF-DAY".equals(existingLog.get().getStatus()) || "LEAVE".equals(existingLog.get().getStatus()))) {
            return ResponseEntity.badRequest().body("Attendance already recorded for this date.");
        }

        AttendanceLog log = existingLog.orElse(new AttendanceLog());
        log.setUserId(user.getId());
        log.setAttendanceDate(reqDate);

        if (request.punchIn != null && !request.punchIn.isEmpty()) {
            LocalDateTime inTime = LocalDateTime.parse(request.date + "T" + request.punchIn + ":00");
            log.setPunchIn(inTime);
        }

        if (request.punchOut != null && !request.punchOut.isEmpty()) {
            LocalDateTime outTime = LocalDateTime.parse(request.date + "T" + request.punchOut + ":00");
            log.setPunchOut(outTime);
        }

        log.setStatus("PENDING_APPROVAL");
        log.setInLocation("Requested: " + request.reason);
        
        attendanceRepository.save(log);
        return ResponseEntity.ok("Past attendance request submitted successfully!");
    }

    @GetMapping("/records")
    public ResponseEntity<?> getAttendanceRecords(
            @RequestParam String email,
            @RequestParam String month) {

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found!");

        User user = userOpt.get();
        boolean isHrOrAdmin = "HR".equalsIgnoreCase(user.getRole()) || "HOD".equalsIgnoreCase(user.getRole());

        LocalDate startDate = LocalDate.parse(month + "-01");
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<AttendanceLog> records;

        if (isHrOrAdmin) {
            records = attendanceRepository.findByAttendanceDateBetweenOrderByAttendanceDateDesc(startDate, endDate);
        } else {
            records = attendanceRepository.findByUserIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(user.getId(), startDate, endDate);
        }

        List<Map<String, Object>> response = new ArrayList<>();

        for (AttendanceLog a : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("employeeId", a.getUserId());

            Optional<User> empOpt = userRepository.findById(a.getUserId());
            map.put("employeeName", empOpt.isPresent() ? empOpt.get().getFullName() : "Unknown Employee");
            map.put("date", a.getAttendanceDate().toString());

            map.put("punchIn", a.getPunchIn() != null ? String.format("%02d:%02d", a.getPunchIn().getHour(), a.getPunchIn().getMinute()) : null);
            map.put("inLocation", a.getInLocation());

            map.put("punchOut", a.getPunchOut() != null ? String.format("%02d:%02d", a.getPunchOut().getHour(), a.getPunchOut().getMinute()) : null);
            map.put("outLocation", a.getOutLocation());

            long workMinutes = 0;
            if (a.getPunchIn() != null && a.getPunchOut() != null) {
                workMinutes = java.time.Duration.between(a.getPunchIn(), a.getPunchOut()).toMinutes();
            }
            map.put("workMinutes", workMinutes);
            map.put("otMinutes", a.getOtHours() != null ? (int)(a.getOtHours() * 60) : 0);
            map.put("lateMinutes", 0);

            String status = a.getStatus() != null ? a.getStatus().toLowerCase().replace(" ", "_") : "absent";
            map.put("status", status);
            map.put("shiftId", "shift-1");

            response.add(map);
        }
        return ResponseEntity.ok(response);
    }
}