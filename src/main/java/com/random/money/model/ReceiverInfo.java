package com.random.money.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ReceiverInfo {
    private Long receiveUserId;    // 받은 사람 ID
    private Long receiveAmount;    // 받은 금액
}
