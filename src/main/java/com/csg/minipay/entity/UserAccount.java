package com.csg.minipay.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "USER_ACCOUNT")
@Getter
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "USER_NO", nullable = false, unique = true, length = 30)
    private String userNo;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    // DB DEFAULT SYSTIMESTAMP 쓰고 싶으면 insertable=false
    @Column(name = "CREATED_AT", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
