package com.mwm.hrms.repository;

import com.mwm.hrms.entity.SalarySlip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SalarySlipRepository extends JpaRepository<SalarySlip, Long> {
    List<SalarySlip> findByUserIdOrderByMonthDesc(Long userId);
    Optional<SalarySlip> findByUserIdAndMonth(Long userId, String month);

    List<SalarySlip> findByMonth(String month);
    List<SalarySlip> findByStatus(String status);
}