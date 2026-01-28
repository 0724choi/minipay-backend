package com.csg.minipay.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENT_EVENT")
@Getter
public class PaymentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EVENT_ID")
    private Long eventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PAYMENT_ID", nullable = false)
    private Payment payment;

    @Column(name = "EVENT_TYPE", nullable = false, length = 20)
    private String eventType; // ISSUE / PAY / CANCEL

    @Column(name = "EVENT_STATUS", nullable = false, length = 20)
    private String eventStatus; // SUCCESS / FAILED

    @Column(name = "REQUEST_ID", nullable = false, unique = true, length = 100)
    private String requestId;

    @Column(name = "AMOUNT")
    private Long amount;

    @Column(name = "CREATED_AT", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ERROR_CODE", length = 50)
    private String errorCode;

    @Column(name = "ERROR_MSG", length = 4000)
    private String errorMsg;

    public static PaymentEvent issueSuccess(Payment payment, String requestId, Long amount) {
        PaymentEvent e = new PaymentEvent();
        e.payment = payment;
        e.eventType = "ISSUE";
        e.eventStatus = "SUCCESS";
        e.requestId = requestId;
        e.amount = amount;
        return e;
    }
    public static PaymentEvent paySuccess(Payment payment, String requestId, Long amount) {
        PaymentEvent e = new PaymentEvent();
        e.payment = payment;
        e.eventType = "PAY";
        e.eventStatus = "SUCCESS";
        e.requestId = requestId;
        e.amount = amount;
        return e;
    }

    public static PaymentEvent payFailed(Payment payment, String requestId, Long amount, String errorCode, String errorMsg) {
        PaymentEvent e = new PaymentEvent();
        e.payment = payment;
        e.eventType = "PAY";
        e.eventStatus = "FAILED";
        e.requestId = requestId;
        e.amount = amount;
        e.errorCode = errorCode;
        e.errorMsg = errorMsg;
        return e;
    }
}
