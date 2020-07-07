package com.random.money.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;
import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "tokenInfos", timeToLive = 600L)
public class TokenInfo implements Serializable {

    @Id
    private String token;          // 뿌리기시 발급되는 token
    private String roomId;         // 뿌린 대화방 ID
    private Long sendUserId;       // 뿌린 사람 ID

}
