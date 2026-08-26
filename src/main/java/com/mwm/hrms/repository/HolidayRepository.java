package com.mwm.hrms.repository;

import com.mwm.hrms.entity.Holiday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    Page<Holiday> findByHolidayDateGreaterThanEqualOrderByHolidayDateAsc(LocalDate date, Pageable pageable);

    List<Holiday> findAllByOrderByHolidayDateAsc();
}