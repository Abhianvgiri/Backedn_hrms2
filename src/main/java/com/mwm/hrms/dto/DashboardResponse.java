package com.mwm.hrms.dto;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardResponse {
    // 1. User Info
    private String userName;
    private String employeeCode;

    // 2. Today's Status
    private boolean punchedIn;
    private boolean punchedOut;
    private String punchInTime;
    private String punchOutTime;
    private String workHours;
    private String todayStatus;

    // 3. Monthly Calendar Data
    private List<Map<String, String>> monthlyAttendance; // [{date: "2026-07-01", status: "present"}]

    // 4. Leave Balance & Stats
    private Map<String, Integer> leaveBalance;
    private int presentDaysThisMonth;

    // 5. Paginated Holidays (Aapki requirement ke hisaab se)
    private List<Map<String, String>> upcomingHolidays;
    private int totalHolidayPages; // Pagination indicator
}