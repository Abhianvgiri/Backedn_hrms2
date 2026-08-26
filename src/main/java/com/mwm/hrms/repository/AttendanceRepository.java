package com.mwm.hrms.repository;

import com.mwm.hrms.entity.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceLog, Long> {
    Optional<AttendanceLog> findByUserIdAndAttendanceDate(Long userId, LocalDate date);
    List<AttendanceLog> findByUserIdAndAttendanceDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<AttendanceLog> findByUserIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(Long userId, LocalDate startDate, LocalDate endDate);
    List<AttendanceLog> findByAttendanceDateBetweenOrderByAttendanceDateDesc(LocalDate startDate, LocalDate endDate);

    long countByUserIdAndAttendanceDateBetweenAndStatus(Long userId, LocalDate startDate, LocalDate endDate, String status);
    void deleteByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);
    List<AttendanceLog> findByStatus(String status);

    // 🔥 NAYI LINE: Is query se direct us mahine ka total OT hours nikal aayega
    @Query("SELECT COALESCE(SUM(a.otHours), 0.0) FROM AttendanceLog a WHERE a.userId = :userId AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Double sumOtHoursByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}