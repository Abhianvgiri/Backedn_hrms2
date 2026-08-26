package com.mwm.hrms.controller;

import com.mwm.hrms.dto.DashboardResponse;
import com.mwm.hrms.entity.AttendanceLog;
import com.mwm.hrms.entity.Holiday;
import com.mwm.hrms.entity.User;
import com.mwm.hrms.entity.SalarySlip;
import com.mwm.hrms.repository.AttendanceRepository;
import com.mwm.hrms.repository.HolidayRepository;
import com.mwm.hrms.repository.UserRepository;
import com.mwm.hrms.repository.SalarySlipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private SalarySlipRepository salarySlipRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getMyDashboard(
            @RequestParam String email,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found!");
        User user = userOpt.get();

        DashboardResponse response = new DashboardResponse();
        response.setUserName(user.getFullName());
        response.setEmployeeCode("MWM-" + user.getId());

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        LocalDate today = LocalDate.now();
        Optional<AttendanceLog> todayLogOpt = attendanceRepository.findByUserIdAndAttendanceDate(user.getId(), today);
        if (todayLogOpt.isPresent()) {
            AttendanceLog log = todayLogOpt.get();
            response.setPunchedIn(log.getPunchIn() != null);
            response.setPunchedOut(log.getPunchOut() != null);
            response.setPunchInTime(log.getPunchIn() != null ? log.getPunchIn().format(timeFormatter) : "--:--");
            response.setPunchOutTime(log.getPunchOut() != null ? log.getPunchOut().format(timeFormatter) : "--:--");
            response.setTodayStatus(log.getStatus());

            if (log.getPunchIn() != null && log.getPunchOut() != null) {
                long mins = java.time.Duration.between(log.getPunchIn(), log.getPunchOut()).toMinutes();
                response.setWorkHours((mins / 60) + "h " + (mins % 60) + "m");
            } else if (log.getPunchIn() != null) {
                long mins = java.time.Duration.between(log.getPunchIn(), java.time.LocalDateTime.now()).toMinutes();
                response.setWorkHours((mins / 60) + "h " + (mins % 60) + "m (Running)");
            } else {
                response.setWorkHours("0h 0m");
            }
        } else {
            response.setPunchInTime("--:--"); response.setPunchOutTime("--:--"); response.setWorkHours("0h 0m"); response.setTodayStatus("Not marked");
        }

        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();

        List<AttendanceLog> monthlyLogs = attendanceRepository.findByUserIdAndAttendanceDateBetween(user.getId(), startDate, endDate);

        List<Map<String, String>> monthlyAttList = new ArrayList<>();
        int presentCount = 0;

        for (AttendanceLog log : monthlyLogs) {
            Map<String, String> dayData = new HashMap<>();
            dayData.put("date", log.getAttendanceDate().toString());
            dayData.put("status", log.getStatus() != null ? log.getStatus().toLowerCase() : "absent");
            monthlyAttList.add(dayData);

            if ("PRESENT".equalsIgnoreCase(log.getStatus())) presentCount++;
        }

        response.setMonthlyAttendance(monthlyAttList);
        response.setPresentDaysThisMonth(presentCount);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Holiday> holidayPage = holidayRepository.findByHolidayDateGreaterThanEqualOrderByHolidayDateAsc(today, pageRequest);

        List<Map<String, String>> holidayList = new ArrayList<>();
        for (Holiday h : holidayPage.getContent()) {
            Map<String, String> map = new HashMap<>();
            map.put("id", String.valueOf(h.getId()));
            map.put("name", h.getName());
            map.put("date", h.getHolidayDate().toString());
            map.put("type", h.getType());
            holidayList.add(map);
        }

        response.setUpcomingHolidays(holidayList);
        response.setTotalHolidayPages(holidayPage.getTotalPages() == 0 ? 1 : holidayPage.getTotalPages());

        Map<String, Integer> leaveBalances = new HashMap<>();
        leaveBalances.put("cl", user.getClBalance() != null ? user.getClBalance().intValue() : 0);
        leaveBalances.put("sl", user.getSlBalance() != null ? user.getSlBalance().intValue() : 0);
        leaveBalances.put("pl", user.getPlBalance() != null ? user.getPlBalance().intValue() : 0);
        leaveBalances.put("lwp", user.getLwpCount() != null ? user.getLwpCount().intValue() : 0);
        response.setLeaveBalance(leaveBalances);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin-stats")
    public ResponseEntity<?> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        List<User> allUsers = userRepository.findAll();
        List<User> activeEmployees = new ArrayList<>();

        for (User u : allUsers) {
            if ("active".equalsIgnoreCase(u.getStatus()) && !"HR".equalsIgnoreCase(u.getRole())) {
                activeEmployees.add(u);
            }
        }

        long totalEmployees = activeEmployees.size();
        stats.put("totalEmployees", totalEmployees);
        
        List<Map<String, Object>> totalEmployeesList = new ArrayList<>();
        for (User u : activeEmployees) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getFullName());
            map.put("empCode", "MWM-" + u.getId());
            map.put("department", u.getDepartment());
            totalEmployeesList.add(map);
        }
        stats.put("totalEmployeesList", totalEmployeesList);

        List<AttendanceLog> todayLogs = attendanceRepository.findByAttendanceDateBetweenOrderByAttendanceDateDesc(today, today);

        Map<Long, AttendanceLog> logMap = new HashMap<>();
        for (AttendanceLog log : todayLogs) {
            logMap.put(log.getUserId(), log);
        }

        long present = 0, late = 0, halfDay = 0, onLeave = 0, absent = 0;

        List<Map<String, Object>> absentUsersList = new ArrayList<>();
        List<Map<String, Object>> lateUsersList = new ArrayList<>();
        List<Map<String, Object>> pendingApprovalsList = new ArrayList<>();
        List<Map<String, Object>> presentUsersList = new ArrayList<>();
        List<Map<String, Object>> halfDayUsersList = new ArrayList<>();
        List<Map<String, Object>> onLeaveUsersList = new ArrayList<>();

        for (User emp : activeEmployees) {
            AttendanceLog log = logMap.get(emp.getId());

            if (log != null && log.getStatus() != null) {
                String status = log.getStatus().toUpperCase();
                if ("PRESENT".equals(status)) {
                    present++;
                    Map<String, Object> u = new HashMap<>();
                    u.put("name", emp.getFullName());
                    u.put("empCode", "MWM-" + emp.getId());
                    u.put("department", emp.getDepartment());
                    u.put("punchIn", log.getPunchIn() != null ? log.getPunchIn().toString() : "--:--");
                    presentUsersList.add(u);
                }
                else if ("LATE".equals(status)) {
                    late++;
                    Map<String, Object> lateU = new HashMap<>();
                    lateU.put("name", emp.getFullName());
                    lateU.put("empCode", "MWM-" + emp.getId());
                    lateU.put("department", emp.getDepartment());
                    lateU.put("punchIn", log.getPunchIn() != null ? log.getPunchIn().toString() : "--:--");
                    lateUsersList.add(lateU);
                }
                else if ("PENDING_APPROVAL".equals(status)) {
                    absent++; 
                }
                else if ("HALF-DAY".equals(status)) {
                    halfDay++;
                    Map<String, Object> u = new HashMap<>();
                    u.put("name", emp.getFullName());
                    u.put("empCode", "MWM-" + emp.getId());
                    u.put("department", emp.getDepartment());
                    halfDayUsersList.add(u);
                }
                else if ("LEAVE".equals(status)) {
                    onLeave++;
                    Map<String, Object> u = new HashMap<>();
                    u.put("name", emp.getFullName());
                    u.put("empCode", "MWM-" + emp.getId());
                    u.put("department", emp.getDepartment());
                    onLeaveUsersList.add(u);
                }
                else {
                    absent++;
                    Map<String, Object> absU = new HashMap<>();
                    absU.put("name", emp.getFullName());
                    absU.put("empCode", "MWM-" + emp.getId());
                    absU.put("department", emp.getDepartment());
                    absentUsersList.add(absU);
                }
            } else {
                absent++;
                Map<String, Object> absU = new HashMap<>();
                absU.put("name", emp.getFullName());
                absU.put("empCode", "MWM-" + emp.getId());
                absU.put("department", emp.getDepartment());
                absentUsersList.add(absU);
            }
        }

        long totalPresent = present + late;
        int attPercent = totalEmployees > 0 ? (int) Math.round(((double) (totalPresent + (halfDay * 0.5)) / totalEmployees) * 100) : 0;

        stats.put("presentToday", totalPresent);
        stats.put("absentToday", absent);
        stats.put("onLeave", onLeave);
        stats.put("halfDay", halfDay);
        stats.put("lateToday", late);
        stats.put("attPercent", attPercent);
        List<AttendanceLog> allPending = attendanceRepository.findByStatus("PENDING_APPROVAL");
        for (AttendanceLog pendLog : allPending) {
            Optional<User> uOpt = userRepository.findById(pendLog.getUserId());
            if (uOpt.isPresent()) {
                User u = uOpt.get();
                Map<String, Object> pendU = new HashMap<>();
                pendU.put("id", pendLog.getId());
                pendU.put("name", u.getFullName());
                pendU.put("empCode", "MWM-" + u.getId());
                pendU.put("department", u.getDepartment());
                pendU.put("date", pendLog.getAttendanceDate().toString());
                pendU.put("punchIn", pendLog.getPunchIn() != null ? pendLog.getPunchIn().toString() : "--:--");
                pendingApprovalsList.add(pendU);
            }
        }

        stats.put("absentUsersList", absentUsersList);
        stats.put("lateUsersList", lateUsersList);
        stats.put("pendingApprovalsList", pendingApprovalsList);
        stats.put("presentUsersList", presentUsersList);
        stats.put("halfDayUsersList", halfDayUsersList);
        stats.put("onLeaveUsersList", onLeaveUsersList);

        String currentMonth = YearMonth.now().toString();
        List<SalarySlip> thisMonthSlips = salarySlipRepository.findByMonth(currentMonth);

        double onlinePay = 0, cashPay = 0, contractualPay = 0;
        int contractualOtCount = 0;
        double contractualOtTotal = 0;
        List<Map<String, Object>> contractualOtUsersList = new ArrayList<>();

        for (SalarySlip slip : thisMonthSlips) {
            if ("CASH".equalsIgnoreCase(slip.getPaymentMode())) {
                cashPay += slip.getNetSalary();
            } else if ("CONTRACTUAL".equalsIgnoreCase(slip.getPaymentMode())) {
                contractualPay += slip.getNetSalary();
                
                double userOtAmount = 0;
                if (slip.getOtPay() > 0) userOtAmount = slip.getOtPay();
                else if (slip.getOtSalary() != null && slip.getOtSalary() > 0) userOtAmount = slip.getOtSalary();

                if (userOtAmount > 0 || slip.getOtHours() > 0) {
                    contractualOtCount++;
                    contractualOtTotal += userOtAmount;

                    Map<String, Object> otUser = new HashMap<>();
                    otUser.put("name", slip.getUser().getFullName());
                    otUser.put("empCode", "MWM-" + slip.getUser().getId());
                    otUser.put("department", slip.getUser().getDepartment());
                    otUser.put("otHours", slip.getOtHours());
                    otUser.put("otAmount", userOtAmount);
                    contractualOtUsersList.add(otUser);
                }
            } else {
                onlinePay += slip.getNetSalary();
            }
        }
        
        stats.put("contractualOtCount", contractualOtCount);
        stats.put("contractualOtTotal", contractualOtTotal);
        stats.put("contractualOtUsersList", contractualOtUsersList);

        Map<String, Double> payroll = new HashMap<>();
        payroll.put("online", onlinePay);
        payroll.put("cash", cashPay);
        payroll.put("contractual", contractualPay);
        payroll.put("total", onlinePay + cashPay + contractualPay);
        stats.put("payrollSummary", payroll);

        Map<String, Integer> deptCounts = new HashMap<>();
        for (User u : activeEmployees) {
            String dept = u.getDepartment() != null ? u.getDepartment().replace("dep-", "").toUpperCase() : "OTHER";
            deptCounts.put(dept, deptCounts.getOrDefault(dept, 0) + 1);
        }

        List<Map<String, Object>> deptChart = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : deptCounts.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", entry.getKey());
            map.put("employees", entry.getValue());
            deptChart.add(map);
        }
        stats.put("departmentChart", deptChart);

        return ResponseEntity.ok(stats);
    }
}