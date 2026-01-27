package com.csg.minipay.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;


@Entity
@Table(name = "PAYMENT")
@Getter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_ID")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UserAccount user;

    @Column(name = "AMOUNT_DUE", nullable = false)
    private Long amountDue;

    @Column(name = "AMOUNT_PAID", nullable = false)
    private Long amountPaid = 0L;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status; // UNPAID / PAID / CANCELED

    @Column(name = "CREATED_AT", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "PAID_AT")
    private LocalDateTime paidAt;

    @Column(name = "CANCELED_AT")
    private LocalDateTime canceledAt;

    @Column(name = "VERSION_NO", nullable = false)
    private Long versionNo = 0L;

    public static Payment issue(UserAccount user, long amountDue) {
        Payment p = new Payment();
        p.user = user;
        p.amountDue = amountDue;
        p.amountPaid = 0L;
        p.status = "UNPAID";
        p.versionNo = 0L;
        return p;
    }
}
