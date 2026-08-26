package com.mwm.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Data
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "leave_type")
    private String leaveType;

    @Column(name = "day_type")
    private String dayType;

    // Frontend uses 'fromDate', but DB uses 'start_date'
    @Column(name = "start_date")
    private LocalDate fromDate;

    // Frontend uses 'toDate', but DB uses 'end_date'
    @Column(name = "end_date")
    private LocalDate toDate;

    private double days;
    private String reason;

    // Default status in DB is PENDING (Uppercase ENUM)
    private String status = "PENDING";

    @Column(name = "created_at")
    private LocalDateTime appliedOn = LocalDateTime.now();

    @Column(name = "approved_by")
    private Long approvedBy;
}