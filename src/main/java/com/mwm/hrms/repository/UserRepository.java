package com.mwm.hrms.repository;

import com.mwm.hrms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Ye line apne aap email ke basis par user ko dhoondh legi
    Optional<User> findByEmail(String email);
}