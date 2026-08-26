package com.mwm.hrms.controller;

import com.mwm.hrms.entity.AttendanceLog;
import com.mwm.hrms.entity.User;
import com.mwm.hrms.repository.AttendanceRepository;
import com.mwm.hrms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
public class TestController {

    @Autowired
    private AttendanceRepository attendanceLogRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/test/insert-june-dummy")
    @Transactional
    public String insertJuneDummyData() {
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 30);

        attendanceLogRepository.deleteByAttendanceDateBetween(startDate, endDate);

        List<User> employees = userRepository.findAll();

        for (User emp : employees) {
            if (!"EMPLOYEE".equalsIgnoreCase(emp.getRole())) continue;

            for (int i = 1; i <= 30; i++) {
                LocalDate currentDate = LocalDate.of(2026, 6, i);
                AttendanceLog log = new AttendanceLog();

                log.setUserId(emp.getId());
                log.setAttendanceDate(currentDate);
                log.setInLocation("Gurugram HQ");
                log.setOutLocation("Gurugram HQ");

                if (i == 7 || i == 14 || i == 21 || i == 28) {
                    log.setStatus("SUNDAY");
                    log.setOtHours(0.0);
                } else if (i == 10 || i == 11) {
                    log.setStatus("ABSENT");
                    log.setOtHours(0.0);
                } else if (i == 15 || i == 16) {
                    log.setStatus("PRESENT");
                    log.setPunchIn(LocalDateTime.of(currentDate, LocalTime.of(9, 0)));
                    log.setPunchOut(LocalDateTime.of(currentDate, LocalTime.of(21, 0)));
                    log.setOtHours(3.0);
                } else {
                    log.setStatus("PRESENT");
                    log.setPunchIn(LocalDateTime.of(currentDate, LocalTime.of(9, 0)));
                    log.setPunchOut(LocalDateTime.of(currentDate, LocalTime.of(18, 0)));
                    log.setOtHours(0.0);
                }

                attendanceLogRepository.save(log);
            }
        }
        return "Boom! 💥 June 2026 (30 days) ka perfect dummy data (OT & LWP ke sath) saare employees ke liye insert ho gaya!";
    }
}