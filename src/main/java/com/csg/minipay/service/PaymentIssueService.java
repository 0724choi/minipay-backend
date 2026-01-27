package com.csg.minipay.service;

import com.csg.minipay.dto.IssueRequest;
import com.csg.minipay.dto.IssueResponse;
import com.csg.minipay.entity.Payment;
import com.csg.minipay.entity.PaymentEvent;
import com.csg.minipay.entity.UserAccount;
import com.csg.minipay.repository.PaymentEventRepository;
import com.csg.minipay.repository.PaymentRepository;
import com.csg.minipay.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentIssueService {

    private final UserAccountRepository userAccountRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository paymentEventRepository;

    @Transactional
    public IssueResponse issue(IssueRequest req) {
        // 0) 간단 유효성
        if (req.userNo() == null || req.userNo().isBlank()) {
            return new IssueResponse("INVALID_INPUT", "USER_NO_REQUIRED", null);
        }
        if (req.requestId() == null || req.requestId().isBlank()) {
            return new IssueResponse("INVALID_INPUT", "REQUEST_ID_REQUIRED", null);
        }
        if (req.amountDue() == null || req.amountDue() <= 0) {
            return new IssueResponse("INVALID_INPUT", "AMOUNT_DUE_MUST_BE_POSITIVE", null);
        }

        // 1) 멱등성: requestId가 이미 있으면 기존 paymentId 반환
        var existed = paymentEventRepository.findByRequestId(req.requestId());
        if (existed.isPresent()) {
            return new IssueResponse(
                    "IDEMPOTENT_OK",
                    "ALREADY_PROCESSED_REQUEST_ID",
                    existed.get().getPayment().getPaymentId()
            );
        }

        // 2) 사용자 조회
        UserAccount user = userAccountRepository.findByUserNo(req.userNo())
                .orElse(null);
        if (user == null) {
            return new IssueResponse("USER_NOT_FOUND", "NO_USER_ROW", null);
        }

        // 3) PAYMENT 생성 (UNPAID)
        Payment payment = paymentRepository.save(Payment.issue(user, req.amountDue()));

        // 4) ISSUE 이벤트 기록
        paymentEventRepository.save(PaymentEvent.issueSuccess(payment, req.requestId(), req.amountDue()));

        return new IssueResponse("OK", "ISSUE_SUCCESS", payment.getPaymentId());
    }
}
