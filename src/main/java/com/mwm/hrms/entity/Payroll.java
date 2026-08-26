package com.mwm.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payroll")
@Data
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private String month; // Format: YYYY-MM

    private Double basic;
    private Double hra;
    private Double allowance;
    private Double bonus = 0.0;

    @Column(name = "ot_pay")
    private Double otPay = 0.0;

    private Double pf;
    private Double esi;
    private Double tax = 0.0;

    @Column(name = "lwp_deduction")
    private Double lwpDeduction = 0.0;

    private Double deductions;
    private Double net;

    @Column(name = "payment_mode")
    private String paymentMode;
    private Double fine = 0.0;
    private String status = "paid";
}