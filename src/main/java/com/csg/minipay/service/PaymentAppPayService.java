package com.csg.minipay.service;

import com.csg.minipay.dto.ProcResult;
import com.csg.minipay.entity.Payment;
import com.csg.minipay.entity.PaymentEvent;
import com.csg.minipay.repository.PaymentEventRepository;
import com.csg.minipay.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentAppPayService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final PaymentEventFailLogger failLogger;

    @Transactional
    public ProcResult payJpa(Long paymentId, String requestId, Long amount) {

        // 0) 유효성
        if (paymentId == null || paymentId <= 0) {
            return new ProcResult("INVALID_INPUT", "PAYMENT_ID_REQUIRED");
        }
        if (requestId == null || requestId.isBlank()) {
            return new ProcResult("INVALID_INPUT", "REQUEST_ID_REQUIRED");
        }
        if (amount == null || amount <= 0) {
            return new ProcResult("INVALID_INPUT", "AMOUNT_MUST_BE_POSITIVE");
        }

        // 1) 멱등성 체크
        var existed = paymentEventRepository.findByRequestId(requestId);
        if (existed.isPresent()) {
            PaymentEvent ev = existed.get();
            if ("PAY".equals(ev.getEventType()) && "SUCCESS".equals(ev.getEventStatus())) {
                return new ProcResult("IDEMPOTENT_OK", "ALREADY_PROCESSED_REQUEST_ID");
            }
            return new ProcResult("DUPLICATE_REQUEST_ID", "REQUEST_ID_ALREADY_USED");
        }

        try {
            // 2) payment row 락
            Payment payment = paymentRepository.findByIdForUpdateNowait(paymentId)
                    .orElse(null);
            if (payment == null) {
                // 롤백할 변경은 없지만, 규칙상 실패 이벤트 남길거면 아래 catch 흐름이 아니라 여기서도 남길 수 있음
                // 지금은 간단히 결과만 반환
                return new ProcResult("PAYMENT_NOT_FOUND", "NO_PAYMENT_ROW");
            }

            // 3) 상태/금액 검증 + 결제 반영
            payment.pay(amount, LocalDateTime.now());

            // 4) 성공 이벤트 기록
            paymentEventRepository.save(PaymentEvent.paySuccess(payment, requestId, amount));

            return new ProcResult("OK", "PAY_SUCCESS");

        } catch (PessimisticLockingFailureException e) {
            // 락 즉시 실패
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            safeFailLog(paymentId, requestId, amount, "LOCK_FAILED", e.getMessage());
            return new ProcResult("LOCK_FAILED", "PAYMENT_ROW_LOCKED");

        } catch (Exception e) {
            // 그 외 실패
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            String code = e.getClass().getSimpleName();
            safeFailLog(paymentId, requestId, amount, code, e.getMessage());
            return new ProcResult("FAILED", code);
        }
    }

    private void safeFailLog(Long paymentId, String requestId, Long amount, String errorCode, String errorMsg) {
        try {
            failLogger.logPayFailed(paymentId, requestId, amount, errorCode, errorMsg);
        } catch (Exception ignore) {
            // fail log까지 실패하면 어쩔 수 없음
        }
    }
}
