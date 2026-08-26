package com.mwm.hrms.controller;

import com.mwm.hrms.dto.PayrollDraftDTO;
import com.mwm.hrms.entity.Loan;
import com.mwm.hrms.entity.SalarySlip;
import com.mwm.hrms.entity.User;
import com.mwm.hrms.repository.AttendanceRepository;
import com.mwm.hrms.repository.LoanRepository;
import com.mwm.hrms.repository.SalarySlipRepository;
import com.mwm.hrms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payroll")
@CrossOrigin(origins = "*")
public class PayrollController {

    @Autowired
    private SalarySlipRepository salarySlipRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private LoanRepository loanRepository;

    @GetMapping("/draft")
    public ResponseEntity<List<PayrollDraftDTO>> getDraftPayroll(@RequestParam String month) {
        List<User> employees = userRepository.findAll();
        YearMonth ym = YearMonth.parse(month);
        int daysInMonth = ym.lengthOfMonth();
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<PayrollDraftDTO> drafts = new ArrayList<>();

        for (User emp : employees) {
            if (emp.getRole() == null || !"EMPLOYEE".equalsIgnoreCase(emp.getRole())) {
                continue;
            }

            PayrollDraftDTO dto = new PayrollDraftDTO();
            dto.setUserId(emp.getId());
            dto.setEmployeeName(emp.getFullName() != null ? emp.getFullName() : "Unknown");
            dto.setEmployeeCode(emp.getEmployeeCode());

            String mode = emp.getPaymentMode() != null ? emp.getPaymentMode().toUpperCase() : "BANK";
            dto.setPaymentMode(mode.equals("ONLINE") ? "BANK" : mode);
            dto.setTotalDays(daysInMonth);

            long present = attendanceRepository.countByUserIdAndAttendanceDateBetweenAndStatus(emp.getId(), startDate, endDate, "PRESENT");
            long sundays = attendanceRepository.countByUserIdAndAttendanceDateBetweenAndStatus(emp.getId(), startDate, endDate, "SUNDAY");
            dto.setWorkedDays((int) (present + sundays));

            // ✅ OT Calculation Zinda hai
            Double totalOt = attendanceRepository.sumOtHoursByUserIdAndDateRange(emp.getId(), startDate, endDate);
            double otHours = totalOt != null ? totalOt : 0.0;
            dto.setOtHours(otHours);

            double totalGross = emp.getFixedGrossSalary() != null ? emp.getFixedGrossSalary() : 0.0;
            dto.setFixedGross(totalGross);
            double specialAllowance = emp.getSpecialAllowance() != null ? emp.getSpecialAllowance() : 0.0;

            double earnedTotalGross = (totalGross / daysInMonth) * dto.getWorkedDays();
            double earnedSpecial = (specialAllowance / daysInMonth) * dto.getWorkedDays();
            double earnedActualGross = earnedTotalGross - earnedSpecial;

            dto.setEarnedGross(Math.round(earnedTotalGross * 100.0) / 100.0);

            double basic = Math.round(earnedActualGross * 0.60);
            double hra = Math.round(earnedActualGross * 0.30);
            double conveyance = Math.round(earnedActualGross * 0.10);

            dto.setBasicPay(basic);
            dto.setHraPay(hra);
            dto.setConveyance(conveyance);
            dto.setSpecialAllowance((double) Math.round(earnedSpecial));

            // ✅ OT ke paise ka calculation
            double otRate = Math.round(totalGross / 30.0 / 8.0);
            double otSalary = Math.round(otHours * otRate);
            dto.setOtRate(otRate);
            dto.setOtPay(otSalary);

            double pf = (basic >= 15000) ? 1800.0 : Math.round(basic * 0.12);
            double esic = Math.min(Math.round(earnedActualGross * 0.0075), 158.0);
            double lwf = emp.getLwf() != null ? emp.getLwf() : 35.0;
            double tds = emp.getTds() != null ? emp.getTds() : 0.0;

            // ✅ Fine Set (Initial value 0.0)
            double fine = 0.0;
            dto.setFine(fine);

            dto.setPfDeduction(pf);
            dto.setEsiDeduction(esic);
            dto.setLwf(lwf);
            dto.setTds(tds);

            List<Loan> activeLoans = loanRepository.findByEmpEmailAndStatus(emp.getEmail(), "APPROVED");
            double totalLoanEmi = 0.0;
            for (Loan loan : activeLoans) {
                if (loan.getRemainingAmount() != null && loan.getRemainingAmount() > 0) {
                    double emiToDeduct = Math.min(loan.getMonthlyEmi(), loan.getRemainingAmount());
                    totalLoanEmi += emiToDeduct;
                }
            }
            dto.setLoanDeduction(totalLoanEmi);

            // ✅ Fine ko Total Deductions mein add kiya gaya
            double totalDeduction = pf + esic + lwf + tds + totalLoanEmi + fine;
            dto.setTotalDeduction(totalDeduction);

            double wageToBePaid = earnedActualGross - totalDeduction;
            double otSpecialToBePaid = otSalary + Math.round(earnedSpecial);

            dto.setWagePayable(wageToBePaid);
            dto.setOtSpecialPayable(otSpecialToBePaid);

            // ✅ Net Salary mein OT jud gaya aur Deductions (with fine) kat gaya
            dto.setNetSalary(wageToBePaid + otSpecialToBePaid);

            drafts.add(dto);
        }
        return ResponseEntity.ok(drafts);
    }

    @PostMapping("/generate-bulk")
    public ResponseEntity<?> generateBulkSlips(@RequestParam String month, @RequestBody List<PayrollDraftDTO> drafts) {
        for (PayrollDraftDTO dto : drafts) {
            User user = userRepository.findById(dto.getUserId()).orElse(null);
            if (user == null) continue;

            SalarySlip slip = salarySlipRepository.findByUserIdAndMonth(user.getId(), month)
                    .orElse(new SalarySlip());

            slip.setUser(user);
            slip.setMonth(month);
            slip.setTotalDays(dto.getTotalDays());
            slip.setWorkedDays(dto.getWorkedDays());

            // ✅ OT Variables Mapped back
            slip.setOtHours(dto.getOtHours() != null ? dto.getOtHours() : 0.0);
            slip.setPaymentMode(dto.getPaymentMode());

            slip.setEarnedGross(dto.getEarnedGross());
            slip.setBasicPay(dto.getBasicPay());
            slip.setHraPay(dto.getHraPay());
            slip.setConveyance(dto.getConveyance());
            slip.setSpecialAllowance(dto.getSpecialAllowance());

            slip.setPfDeduction(dto.getPfDeduction());
            slip.setEsiDeduction(dto.getEsiDeduction());
            slip.setLwf(dto.getLwf());
            slip.setTds(dto.getTds());
            slip.setLoanDeduction(dto.getLoanDeduction());

            // ✅ Fine Mapped
            slip.setFine(dto.getFine() != null ? dto.getFine() : 0.0);
            slip.setTotalDeduction(dto.getTotalDeduction());

            // ✅ OT Pay variables mapped
            slip.setOtRate(dto.getOtRate());
            slip.setOtSalary(dto.getOtPay());
            slip.setWagePayable(dto.getWagePayable());
            slip.setOtSpecialPayable(dto.getOtSpecialPayable());

            slip.setNetSalary(dto.getNetSalary());
            slip.setStatus("GENERATED");

            salarySlipRepository.save(slip);

            if (dto.getLoanDeduction() != null && dto.getLoanDeduction() > 0) {
                List<Loan> activeLoans = loanRepository.findByEmpEmailAndStatus(user.getEmail(), "APPROVED");
                double amountToDeduct = dto.getLoanDeduction();

                for (Loan loan : activeLoans) {
                    if (loan.getRemainingAmount() > 0 && amountToDeduct > 0) {
                        double emi = Math.min(loan.getMonthlyEmi(), loan.getRemainingAmount());
                        double deducted = Math.min(emi, amountToDeduct);

                        loan.setRemainingAmount(loan.getRemainingAmount() - deducted);
                        amountToDeduct -= deducted;

                        loanRepository.save(loan);
                    }
                }
            }
        }
        return ResponseEntity.ok("Successfully Generated Detailed Slips for " + month + " & Deducted EMIs");
    }

    @GetMapping("/my-slips")
    public ResponseEntity<?> getMySlips(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(salarySlipRepository.findByUserIdOrderByMonthDesc(user.getId())))
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/request-download")
    public ResponseEntity<?> requestDownload(@RequestParam Long slipId) {
        Optional<SalarySlip> slipOpt = salarySlipRepository.findById(slipId);
        if (slipOpt.isPresent()) {
            SalarySlip slip = slipOpt.get();
            slip.setStatus("DOWNLOAD_REQUESTED");
            salarySlipRepository.save(slip);
            return ResponseEntity.ok("Request sent to HR for approval.");
        }
        return ResponseEntity.badRequest().body("Slip not found.");
    }

    @PostMapping("/approve-download")
    public ResponseEntity<?> approveDownload(@RequestParam Long slipId, @RequestParam String status) {
        Optional<SalarySlip> slipOpt = salarySlipRepository.findById(slipId);
        if (slipOpt.isPresent()) {
            SalarySlip slip = slipOpt.get();
            slip.setStatus("APPROVED".equalsIgnoreCase(status) ? "APPROVED" : "GENERATED");
            salarySlipRepository.save(slip);
            return ResponseEntity.ok("Slip download request " + status);
        }
        return ResponseEntity.badRequest().body("Slip not found.");
    }

    @GetMapping("/pending-downloads")
    public ResponseEntity<?> getPendingDownloads() {
        return ResponseEntity.ok(salarySlipRepository.findByStatus("DOWNLOAD_REQUESTED"));
    }
}