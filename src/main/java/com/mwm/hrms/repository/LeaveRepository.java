package com.mwm.hrms.repository;

import com.mwm.hrms.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByUserIdOrderByAppliedOnDesc(Long userId);
    List<LeaveRequest> findAllByOrderByAppliedOnDesc();
}