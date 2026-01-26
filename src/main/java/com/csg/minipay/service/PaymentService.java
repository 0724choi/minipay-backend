package com.csg.minipay.service;

import com.csg.minipay.dto.ProcResult;
import com.csg.minipay.repository.PaymentProcedureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentProcedureRepository repository;

    // DB-managed 트랜잭션: DB 프로시저가 COMMIT/ROLLBACK 하므로 @Transactional 제거 권장
    public ProcResult pay(Long paymentId, String requestId, Long amount) {
        return repository.payDbManaged(paymentId, requestId, amount);
    }
}
