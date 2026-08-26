package com.mwm.hrms.repository;

import com.mwm.hrms.entity.OfficialDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<OfficialDocument, Long> {
    List<OfficialDocument> findByUserIdOrderByIssuedAtDesc(Long userId);
    List<OfficialDocument> findAllByOrderByIssuedAtDesc();
}