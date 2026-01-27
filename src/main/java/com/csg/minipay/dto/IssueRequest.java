package com.csg.minipay.dto;

public record IssueRequest(
        String userNo,
        String requestId,
        Long amountDue
) {}
