package com.csg.minipay.service;

import com.csg.minipay.dto.ProcResult;
import com.csg.minipay.repository.PaymentProcedureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentProcedureRepository repository;

    @Transactional
    public ProcResult pay(Long paymentId, String requestId, Long amount) {
        return repository.payApp(paymentId, requestId, amount);
    }
}
