package com.mwm.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

// Ye import zaroori hai JSON mapping ke liye
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "holidays")
@Data
public class Holiday {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // YAHAN MAGIC HAI: React se 'date' aayega/jayega, par Java isko 'holidayDate' manega
    @JsonProperty("date")
    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(nullable = false)
    private String type; // national, festival, company

    private String description;
}