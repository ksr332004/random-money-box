package com.random.money.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "send_user_id", nullable = false)
    private Long sendUserId;       // 뿌린 사람 ID

    @Column(name = "room_id", length = 200, nullable = false)
    private String roomId;         // 뿌린 대화방 ID

    @Column(name = "token", length = 3, nullable = false)
    private String token;          // 뿌리기시 발급되는 token

    @Column(name = "throw_amt", nullable = false)
    private Long throwAmount;      // 뿌린 금액

    @CreationTimestamp
    @Column(name = "throw_date_time", nullable = false)
    private LocalDateTime throwDateTime; // 뿌린 시간

    @Column(name = "receiver_count", nullable = false)
    private Long receiverCount;          // 받을 인원

    @JoinColumn(name = "user_id")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Receiver> receivers = new ArrayList<>();

}
