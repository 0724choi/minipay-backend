package com.csg.minipay.controller;

import com.csg.minipay.dto.PayRequest;
import com.csg.minipay.dto.ProcResult;
import com.csg.minipay.service.PaymentAppPayService;
import com.csg.minipay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentAppPayService paymentAppPayService;
    
    @PostMapping("/pay")
    public ProcResult pay(@RequestBody PayRequest request) {
        return paymentService.pay(
                request.getPaymentId(),
                request.getRequestId(),
                request.getAmount()
        );
    }
    
    @PostMapping("/pay-jpa")
    public ProcResult payJpa(@RequestBody PayRequest request) {
        return paymentAppPayService.payJpa(
                request.getPaymentId(),
                request.getRequestId(),
                request.getAmount()
        );
    } 
}

