package com.mwm.hrms.repository;

import com.mwm.hrms.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByMonth(String month);
    void deleteByMonth(String month); // Regenerate karne ke liye
}