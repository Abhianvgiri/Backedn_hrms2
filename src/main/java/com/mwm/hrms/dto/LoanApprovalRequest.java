package com.mwm.hrms.dto;

import lombok.Data;

@Data
public class LoanApprovalRequest {
    private Double approvedAmount;
    private Double monthlyEmi;
    private String paymentMode;
}