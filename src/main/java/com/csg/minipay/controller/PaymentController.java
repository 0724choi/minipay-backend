package com.csg.minipay.controller;

import com.csg.minipay.dto.CancelRequest;
import com.csg.minipay.dto.PayRequest;
import com.csg.minipay.dto.ProcResult;
import com.csg.minipay.service.PaymentAppCancelService;
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
    private final PaymentAppCancelService paymentAppCancelService;
    
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
    
    @PostMapping("/cancel")
    public ProcResult cancel(@RequestBody CancelRequest request) {
        return paymentService.cancel(
                request.getPaymentId(),
                request.getRequestId()
        );
    }

    @PostMapping("/cancel-jpa")
    public ProcResult cancelJpa(@RequestBody CancelRequest request) {
        return paymentAppCancelService.cancelJpa(
                request.getPaymentId(),
                request.getRequestId()
        );
    }
}

