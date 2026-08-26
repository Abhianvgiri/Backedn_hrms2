package com.mwm.hrms.dto;

import lombok.Data;

@Data
public class PunchRequest {
    private String email; // Employee ko identify karne ke liye
    private double latitude;
    private double longitude;
}