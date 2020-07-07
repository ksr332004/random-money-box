package com.random.money.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ReceiveMoneyDTO {

    @Getter
    @ToString
    public static class Req {
        @NotNull
        @Size(min = 3, max = 3)
        private String token;          // 뿌리기시 발급되는 token
    }

    @Data
    @Builder
    public static class Res {
        private Long receiveAmount;    // 받은 금액
    }

}
