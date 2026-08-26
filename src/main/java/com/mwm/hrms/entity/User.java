package com.mwm.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("code")
    @Column(name = "employee_code", unique = true, nullable = false)
    private String employeeCode;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    @Column(nullable = false)
    private String role;

    @Column(name = "payment_mode")
    private String paymentMode;

    @JsonProperty("shiftId")
    @Column(name = "shift_timing")
    private String shiftTiming;

    @JsonProperty("departmentId")
    private String department;

    private String designation;

    @JsonProperty("salary")
    @Column(name = "base_salary")
    private Double baseSalary;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "fixed_gross_salary")
    private Double fixedGrossSalary;

    private String status;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", updatable = false, insertable = false)
    private LocalDateTime updatedAt;


    private String fatherName;
    private LocalDate dob;
    private String bloodGroup;
    private String maritalStatus;
    private String qualification;
    private String nationality = "INDIAN";

    private String aadharNo;
    private String uanNo;
    private String esicNo;
    private String voterId;
    private String pan;
    private String pfNumber;

    private String bankName;
    private String bankAccountName;
    private String accountNo;
    private String ifscCode;

    private String emergencyName;
    private String emergencyRelation;
    private String emergencyPhone;

    @Column(name = "work_place")
    private String workPlace;

    private Double conveyance;

    @Column(name = "special_allowance")
    private Double specialAllowance;

    private Double lwf = 35.0;
    private Double tds = 0.0;

    @Column(name = "cl_balance")
    private Double clBalance = 12.0;

    @Column(name = "sl_balance")
    private Double slBalance = 12.0;

    @Column(name = "pl_balance")
    private Double plBalance = 15.0;

    @Column(name = "lwp_count")
    private Double lwpCount = 0.0; // Without pay leaves count

}