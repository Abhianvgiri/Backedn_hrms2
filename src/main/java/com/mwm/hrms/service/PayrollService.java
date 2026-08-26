package com.mwm.hrms.service;

import com.mwm.hrms.entity.SalarySlip;
import com.mwm.hrms.entity.User;
import com.mwm.hrms.repository.SalarySlipRepository;
import com.mwm.hrms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PayrollService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SalarySlipRepository salarySlipRepository;

    public SalarySlip generatePayrollForUser(Long userId, String month, int totalDays, int workedDays, double otHours, double fine) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<SalarySlip> existingSlip = salarySlipRepository.findByUserIdAndMonth(userId, month);
        SalarySlip slip = existingSlip.orElse(new SalarySlip());

        double fixedGross = user.getFixedGrossSalary() != null ? user.getFixedGrossSalary() : 0.0;
        String mode = user.getPaymentMode() != null ? user.getPaymentMode().toUpperCase() : "BANK";

        double earnedGross = (fixedGross / totalDays) * workedDays;

        double earnedBasic = earnedGross * 0.60;
        double earnedHra = earnedGross * 0.30;
        double earnedOther = earnedGross * 0.10;

        slip.setUser(user);
        slip.setMonth(month);
        slip.setTotalDays(totalDays);
        slip.setWorkedDays(workedDays);
        slip.setOtHours(otHours);
        slip.setPaymentMode(mode);

        slip.setFine(fine);

        slip.setEarnedGross(Math.round(earnedGross));
        slip.setBasicPay(Math.round(earnedBasic));
        slip.setHraPay(Math.round(earnedHra));
        slip.setOtherPay(Math.round(earnedOther));

        double totalDeductions = 0.0;
        double netSalary = 0.0;
        double otPay = 0.0;

        if ("BANK".equals(mode) || "CONTRACTUAL".equals(mode)) {

            double pfApplicableBasic = Math.min(15000, earnedBasic);
            double pf = Math.round(pfApplicableBasic * 0.12);

            double esi = (earnedGross > 17000) ? 35.0 : Math.round(earnedGross * 0.002);

            totalDeductions = pf + esi + fine;
            netSalary = earnedGross - totalDeductions;

            slip.setPfDeduction(pf);
            slip.setEsiDeduction(esi);
            slip.setTotalDeduction(totalDeductions);
            slip.setOtPay(0.0);

        } else if ("CASH".equals(mode)) {

            double hourlyRate = fixedGross / 30.0 / 8.0;
            otPay = Math.round(hourlyRate * otHours);

            totalDeductions = fine;

            netSalary = earnedGross + otPay - totalDeductions;

            slip.setPfDeduction(0.0);
            slip.setEsiDeduction(0.0);
            slip.setTotalDeduction(totalDeductions);
            slip.setOtPay(otPay);
        }

        slip.setNetSalary(Math.round(netSalary));
        slip.setStatus("GENERATED");

        return salarySlipRepository.save(slip);
    }
}