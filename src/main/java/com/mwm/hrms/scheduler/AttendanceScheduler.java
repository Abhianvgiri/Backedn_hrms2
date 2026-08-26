package com.mwm.hrms.scheduler;

import com.mwm.hrms.entity.AttendanceLog;
import com.mwm.hrms.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class AttendanceScheduler {

    @Autowired
    private AttendanceRepository attendanceRepository;

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void autoRejectPendingAttendance() {
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        
        // Find all pending approvals older than 3 days
        List<AttendanceLog> pendingLogs = attendanceRepository.findByStatus("PENDING_APPROVAL");
        
        int rejectedCount = 0;
        for (AttendanceLog log : pendingLogs) {
            if (log.getAttendanceDate().isBefore(threeDaysAgo) || log.getAttendanceDate().isEqual(threeDaysAgo)) {
                log.setStatus("REJECTED");
                attendanceRepository.save(log);
                rejectedCount++;
            }
        }
        
        if (rejectedCount > 0) {
            System.out.println("Auto-rejected " + rejectedCount + " attendance requests older than 3 days.");
        }
    }
}
