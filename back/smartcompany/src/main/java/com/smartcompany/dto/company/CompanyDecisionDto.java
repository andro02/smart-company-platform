package com.smartcompany.dto.company;

public record CompanyDecisionDto(
    boolean approve,
    String rejectionReason // obavezno ako approve = false
) {}