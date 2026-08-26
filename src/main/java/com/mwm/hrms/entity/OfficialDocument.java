package com.mwm.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "official_documents")
@Data
public class OfficialDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String title;

    @Column(name = "document_type", nullable = false)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "issued_at", nullable = false)
    private LocalDate issuedAt = LocalDate.now();

    @Column(name = "file_url")
    private String fileUrl;
}