package com.csg.minipay.controller;

import com.csg.minipay.dto.PayRequest;
import com.csg.minipay.dto.ProcResult;
import com.csg.minipay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public ProcResult pay(@RequestBody PayRequest request) {
        return paymentService.pay(
                request.getPaymentId(),
                request.getRequestId(),
                request.getAmount()
        );
    }
}
