package com.csg.minipay;

import com.csg.minipay.dto.IssueRequest;
import com.csg.minipay.dto.IssueResponse;
import com.csg.minipay.dto.ProcResult;
import com.csg.minipay.entity.Payment;
import com.csg.minipay.entity.PaymentEvent;
import com.csg.minipay.repository.PaymentRepository;
import com.csg.minipay.service.PaymentAppCancelService;
import com.csg.minipay.service.PaymentAppPayService;
import com.csg.minipay.service.PaymentIssueService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Transactional 
class PaymentFlowIntegrationTest {

    @Autowired PaymentIssueService paymentIssueService;
    @Autowired PaymentAppPayService paymentAppPayService;
    @Autowired PaymentAppCancelService paymentAppCancelService;

    @Autowired PaymentRepository paymentRepository;

    @PersistenceContext EntityManager em;

    @Test
    void issue_then_payJpa_then_cancelJpa_success() {
        // given: ISSUE
        String issueReqId = "TEST-ISSUE-" + UUID.randomUUID();
        IssueResponse issueRes = paymentIssueService.issue(new IssueRequest("U002", issueReqId, 1000L));

        assertThat(issueRes.code()).isIn("OK", "IDEMPOTENT_OK");
        Long paymentId = issueRes.paymentId();
        assertThat(paymentId).isNotNull();

        // when: PAY-JPA
        String payReqId = "TEST-PAY-" + UUID.randomUUID();
        ProcResult payRes = paymentAppPayService.payJpa(paymentId, payReqId, 1000L);
        assertThat(payRes.getCode()).isEqualTo("OK");

        // when: CANCEL-JPA
        String cancelReqId = "TEST-CANCEL-" + UUID.randomUUID();
        ProcResult cancelRes = paymentAppCancelService.cancelJpa(paymentId, cancelReqId);
        assertThat(cancelRes.getCode()).isEqualTo("OK");

        // then: PAYMENT 상태 검증
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo("CANCELED");
        assertThat(payment.getAmountPaid()).isEqualTo(0L);
        assertThat(payment.getCanceledAt()).isNotNull();

        // then: 이벤트 3개(ISSUE/PAY/CANCEL) 검증 (리포지토리에 메서드 추가 안 하고 JPQL로 조회)
        List<PaymentEvent> events = em.createQuery(
                        "select e from PaymentEvent e where e.payment.paymentId = :pid order by e.eventId",
                        PaymentEvent.class)
                .setParameter("pid", paymentId)
                .getResultList();

        assertThat(events).hasSize(3);
        assertThat(events.stream().map(PaymentEvent::getEventType))
                .containsExactly("ISSUE", "PAY", "CANCEL");
        assertThat(events.stream().map(PaymentEvent::getEventStatus))
                .allMatch(s -> "SUCCESS".equals(s));
    }
    
    @Test
    void cancel_idempotency_same_requestId_called_twice_should_be_idempotent() {
        // given: ISSUE
        String issueReqId = "TEST-ISSUE-" + UUID.randomUUID();
        IssueResponse issueRes = paymentIssueService.issue(new IssueRequest("U002", issueReqId, 1000L));
        Long paymentId = issueRes.paymentId();

        // when: CANCEL-JPA 1회차
        String cancelReqId = "TEST-CANCEL-IDEMP-" + UUID.randomUUID();
        ProcResult first = paymentAppCancelService.cancelJpa(paymentId, cancelReqId);
        assertThat(first.getCode()).isEqualTo("OK");

        // when: CANCEL-JPA 2회차 (같은 requestId)
        ProcResult second = paymentAppCancelService.cancelJpa(paymentId, cancelReqId);
        assertThat(second.getCode()).isIn("IDEMPOTENT_OK", "OK"); // 정책에 따라 OK로 와도 허용

        // then: 이벤트는 CANCEL 1건만
        List<PaymentEvent> events = em.createQuery(
                        "select e from PaymentEvent e where e.payment.paymentId = :pid and e.eventType = 'CANCEL'",
                        PaymentEvent.class)
                .setParameter("pid", paymentId)
                .getResultList();

        assertThat(events).hasSize(1);
    }

}


