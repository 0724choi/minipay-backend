package com.csg.minipay.dto;

public record IssueResponse(
        String code,
        String message,
        Long paymentId
) {}
