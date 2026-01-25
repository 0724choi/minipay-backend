package com.csg.minipay.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayRequest {
    private Long paymentId;
    private String requestId;
    private Long amount;
}
