package com.mwm.hrms.repository;

import com.mwm.hrms.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByEmpEmailOrderByAppliedDateDesc(String empEmail);

    List<Loan> findAllByOrderByAppliedDateDesc();
    List<Loan> findByEmpEmailAndStatus(String empEmail, String status);
}