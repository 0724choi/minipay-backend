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
public class PaymentAppCancelService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final PaymentEventFailLogger failLogger;

    @Transactional
    public ProcResult cancelJpa(Long paymentId, String requestId) {

        // 0) 유효성
        if (paymentId == null || paymentId <= 0) {
            return new ProcResult("INVALID_INPUT", "PAYMENT_ID_REQUIRED");
        }
        if (requestId == null || requestId.isBlank()) {
            return new ProcResult("INVALID_INPUT", "REQUEST_ID_REQUIRED");
        }

        // 1) 멱등성 체크
        var existed = paymentEventRepository.findByRequestId(requestId);
        if (existed.isPresent()) {
            PaymentEvent ev = existed.get();
            if ("CANCEL".equals(ev.getEventType()) && "SUCCESS".equals(ev.getEventStatus())) {
                return new ProcResult("IDEMPOTENT_OK", "ALREADY_PROCESSED_REQUEST_ID");
            }
            return new ProcResult("DUPLICATE_REQUEST_ID", "REQUEST_ID_ALREADY_USED");
        }

        Long cancelAmountForEvent = 0L;

        try {
            // 2) payment row 락
            Payment payment = paymentRepository.findByIdForUpdateNowait(paymentId)
                    .orElse(null);
            if (payment == null) {
                return new ProcResult("PAYMENT_NOT_FOUND", "NO_PAYMENT_ROW");
            }

            // 3) 취소 이벤트 amount: 취소 전 상태 기준
            if ("PAID".equals(payment.getStatus())) {
                cancelAmountForEvent = payment.getAmountPaid();
            } else {
                cancelAmountForEvent = 0L;
            }

            // 4) 상태 검증 + 취소 반영
            payment.cancel(LocalDateTime.now());

            // 5) 성공 이벤트 기록
            paymentEventRepository.save(
                    PaymentEvent.cancelSuccess(payment, requestId, cancelAmountForEvent)
            );

            return new ProcResult("OK", "CANCEL_SUCCESS");

        } catch (PessimisticLockingFailureException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            safeFailLog(paymentId, requestId, cancelAmountForEvent, "LOCK_FAILED", e.getMessage());
            return new ProcResult("LOCK_FAILED", "PAYMENT_ROW_LOCKED");

        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            String code = e.getClass().getSimpleName();
            safeFailLog(paymentId, requestId, cancelAmountForEvent, code, e.getMessage());
            return new ProcResult("FAILED", code);
        }
    }

    private void safeFailLog(Long paymentId, String requestId, Long amount, String errorCode, String errorMsg) {
        try {
            failLogger.logCancelFailed(paymentId, requestId, amount, errorCode, errorMsg);
        } catch (Exception ignore) {
        }
    }
}
