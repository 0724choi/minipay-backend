package com.csg.minipay.controller;

import com.csg.minipay.dto.IssueRequest;
import com.csg.minipay.dto.IssueResponse;
import com.csg.minipay.service.PaymentIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentIssueController {

    private final PaymentIssueService paymentIssueService;

    @PostMapping("/issue")
    public IssueResponse issue(@RequestBody IssueRequest req) {
        return paymentIssueService.issue(req);
    }
}
