package com.mwm.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data // Agar Lombok nahi hai, toh Getters/Setters generate kar lena
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String empName;
    private String empCode;
    private String empEmail; // User ko identify karne ke liye

    private Double reqAmount;
    private String reason;

    private String status; // PENDING, APPROVED, REJECTED

    private Double approvedAmount;
    private Double monthlyEmi;
    private String paymentMode; // BANK, CASH, CHEQUE

    private Double remainingAmount; // ✅ NAYA: Bacha hua paisa track karne ke liye
    private LocalDateTime appliedDate;
}