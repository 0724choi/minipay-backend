package com.csg.minipay.service;

import com.csg.minipay.entity.Payment;
import com.csg.minipay.entity.PaymentEvent;
import com.csg.minipay.repository.PaymentEventRepository;
import com.csg.minipay.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentEventFailLogger {

    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository paymentEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPayFailed(Long paymentId, String requestId, Long amount, String errorCode, String errorMsg) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("PAYMENT_NOT_FOUND_FOR_FAIL_LOG"));

        paymentEventRepository.save(
                PaymentEvent.payFailed(payment, requestId, amount, errorCode, errorMsg)
        );
    }
}
