package com.mwm.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "salary_slips")
@Data
public class SalarySlip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    private String month;
    private int totalDays;
    private int workedDays;

    // ✅ OT Zinda hai
    private double otHours;
    private double otPay;
    private Double otRate;
    private Double otSalary;
    private Double otSpecialPayable;

    private double basicPay;
    private double hraPay;
    private double otherPay;
    private double earnedGross;

    private double pfDeduction;
    private double esiDeduction;
    private double totalDeduction;

    // ✅ Naya Fine
    private Double fine = 0.0;

    private double netSalary;
    private String paymentMode;
    private String status = "GENERATED";

    private Double conveyance;
    private Double specialAllowance;
    private Double lwf;
    private Double tds;
    private Double wagePayable;
    private double loanDeduction;
}