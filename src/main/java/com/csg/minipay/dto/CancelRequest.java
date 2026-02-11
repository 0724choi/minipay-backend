package com.csg.minipay.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelRequest {
    private Long paymentId;
    private String requestId;
}
