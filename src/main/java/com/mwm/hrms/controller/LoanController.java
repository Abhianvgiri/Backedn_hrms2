package com.mwm.hrms.controller;

import com.mwm.hrms.dto.LoanApprovalRequest;
import com.mwm.hrms.entity.Loan;
import com.mwm.hrms.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*") // CORS error se bachne ke liye
public class LoanController {

    @Autowired
    private LoanRepository loanRepository;

    // 1. APPLY FOR LOAN (Employee)
    @PostMapping("/apply")
    public ResponseEntity<Loan> applyForLoan(@RequestBody Loan loanRequest) {
        loanRequest.setStatus("PENDING");
        loanRequest.setAppliedDate(LocalDateTime.now());
        loanRequest.setApprovedAmount(0.0);
        loanRequest.setMonthlyEmi(0.0);

        Loan savedLoan = loanRepository.save(loanRequest);
        return ResponseEntity.ok(savedLoan);
    }

    // 2. GET LOANS (HR gets all, Employee gets their own)
    @GetMapping
    public ResponseEntity<List<Loan>> getLoans(@RequestParam String email, @RequestParam String role) {
        if ("hr".equalsIgnoreCase(role) || "hod".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(loanRepository.findAllByOrderByAppliedDateDesc());
        } else {
            return ResponseEntity.ok(loanRepository.findByEmpEmailOrderByAppliedDateDesc(email));
        }
    }

    // 3. APPROVE LOAN (HR/HOD)
    @PutMapping("/{id}/approve")
    public ResponseEntity<Loan> approveLoan(@PathVariable Long id, @RequestBody LoanApprovalRequest approvalData) {
        Loan loan = loanRepository.findById(id).orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("APPROVED");
        loan.setApprovedAmount(approvalData.getApprovedAmount());

        // ✅ NAYA: Shuru mein bacha hua amount = approved amount
        loan.setRemainingAmount(approvalData.getApprovedAmount());

        loan.setMonthlyEmi(approvalData.getMonthlyEmi());
        loan.setPaymentMode(approvalData.getPaymentMode());

        return ResponseEntity.ok(loanRepository.save(loan));
    }

    // 4. REJECT LOAN (HR/HOD)
    @PutMapping("/{id}/reject")
    public ResponseEntity<Loan> rejectLoan(@PathVariable Long id) {
        Loan loan = loanRepository.findById(id).orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("REJECTED");

        return ResponseEntity.ok(loanRepository.save(loan));
    }
}