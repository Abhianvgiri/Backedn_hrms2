package com.mwm.hrms.dto;

import lombok.Data;

@Data
public class PayrollDraftDTO {
    private Long userId;
    private String employeeName;
    private String employeeCode;
    private String paymentMode;
    private Double fixedGross;
    private Integer totalDays;
    private Integer workedDays;

    // ✅ OT Zinda hai
    private Double otHours;
    private Double otPay;
    private Double otRate = 0.0;
    private Double otSpecialPayable = 0.0;

    private Double basicPay;
    private Double hraPay;
    private Double otherPay;
    private Double earnedGross;
    private Double pfDeduction;
    private Double esiDeduction;
    private Double totalDeduction;

    // ✅ Naya Fine variable
    private Double fine = 0.0;

    private Double conveyance = 0.0;
    private Double specialAllowance = 0.0;
    private Double lwf = 0.0;
    private Double tds = 0.0;
    private Double wagePayable = 0.0;

    private Double loanDeduction = 0.0;
    private Double netSalary;

    // (Aap apne IDE/Lombok se inke Getters/Setters generate kar lijiye)


    public Long getUserId() {

        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public Double getFixedGross() {
        return fixedGross;
    }

    public void setFixedGross(Double fixedGross) {
        this.fixedGross = fixedGross;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public Integer getWorkedDays() {
        return workedDays;
    }

    public void setWorkedDays(Integer workedDays) {
        this.workedDays = workedDays;
    }

    public Double getOtHours() {
        return otHours;
    }

    public void setOtHours(Double otHours) {
        this.otHours = otHours;
    }

    public Double getOtPay() {
        return otPay;
    }

    public void setOtPay(Double otPay) {
        this.otPay = otPay;
    }

    public Double getOtRate() {
        return otRate;
    }

    public void setOtRate(Double otRate) {
        this.otRate = otRate;
    }

    public Double getOtSpecialPayable() {
        return otSpecialPayable;
    }

    public void setOtSpecialPayable(Double otSpecialPayable) {
        this.otSpecialPayable = otSpecialPayable;
    }

    public Double getBasicPay() {
        return basicPay;
    }

    public void setBasicPay(Double basicPay) {
        this.basicPay = basicPay;
    }

    public Double getHraPay() {
        return hraPay;
    }

    public void setHraPay(Double hraPay) {
        this.hraPay = hraPay;
    }

    public Double getOtherPay() {
        return otherPay;
    }

    public void setOtherPay(Double otherPay) {
        this.otherPay = otherPay;
    }

    public Double getEarnedGross() {
        return earnedGross;
    }

    public void setEarnedGross(Double earnedGross) {
        this.earnedGross = earnedGross;
    }

    public Double getPfDeduction() {
        return pfDeduction;
    }

    public void setPfDeduction(Double pfDeduction) {
        this.pfDeduction = pfDeduction;
    }

    public Double getEsiDeduction() {
        return esiDeduction;
    }

    public void setEsiDeduction(Double esiDeduction) {
        this.esiDeduction = esiDeduction;
    }

    public Double getTotalDeduction() {
        return totalDeduction;
    }

    public void setTotalDeduction(Double totalDeduction) {
        this.totalDeduction = totalDeduction;
    }

    public Double getFine() {
        return fine;
    }

    public void setFine(Double fine) {
        this.fine = fine;
    }

    public Double getConveyance() {
        return conveyance;
    }

    public void setConveyance(Double conveyance) {
        this.conveyance = conveyance;
    }

    public Double getSpecialAllowance() {
        return specialAllowance;
    }

    public void setSpecialAllowance(Double specialAllowance) {
        this.specialAllowance = specialAllowance;
    }

    public Double getLwf() {
        return lwf;
    }

    public void setLwf(Double lwf) {
        this.lwf = lwf;
    }

    public Double getTds() {
        return tds;
    }

    public void setTds(Double tds) {
        this.tds = tds;
    }

    public Double getWagePayable() {
        return wagePayable;
    }

    public void setWagePayable(Double wagePayable) {
        this.wagePayable = wagePayable;
    }

    public Double getLoanDeduction() {
        return loanDeduction;
    }

    public void setLoanDeduction(Double loanDeduction) {
        this.loanDeduction = loanDeduction;
    }

    public Double getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(Double netSalary) {
        this.netSalary = netSalary;
    }
}