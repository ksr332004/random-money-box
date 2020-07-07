package com.random.money.service;

import com.random.money.entity.Receiver;
import com.random.money.entity.Transaction;
import com.random.money.exception.BusinessException;
import com.random.money.exception.ErrorCode;
import com.random.money.model.ReceiverInfo;
import com.random.money.model.ThrowMoneyInfo;
import com.random.money.model.TokenInfo;
import com.random.money.model.Transactions;
import com.random.money.repository.ReceiverRepository;
import com.random.money.repository.TokenInfoRedisRepository;
import com.random.money.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MoneyService {
    private static final int TOKEN_SIZE = 3;

    private final TransactionRepository transactionRepository;
    private final ReceiverRepository receiverRepository;
    private final TokenInfoRedisRepository tokenInfoRedisRepository;

    private static final Random random = new Random();

    @Transactional
    public String setThrowMoneyInfo(Long sendUserId, String roomId, ThrowMoneyInfo throwMoneyInfo) {

        // get token
        final String token = getToken();

        // cache token
        tokenInfoRedisRepository.save(
                TokenInfo.builder()
                        .token(token)
                        .sendUserId(sendUserId)
                        .roomId(roomId)
                        .build());

        // save transaction
        final Transaction transaction = Transaction.builder()
                .sendUserId(sendUserId)
                .roomId(roomId)
                .token(token)
                .throwAmount(throwMoneyInfo.getThrowAmount())
                .receiverCount(throwMoneyInfo.getReceiverCount())
                .build();

        transactionRepository.save(transaction);

        // save receiver
        Long throwAmount = throwMoneyInfo.getThrowAmount();
        for (int i = 0; i < throwMoneyInfo.getReceiverCount() - 1; i++) {
            long receiveAmount = throwAmount * random.nextInt(70) / 100;

            receiverRepository.save(Receiver.builder()
                    .token(token)
                    .receiveAmount(receiveAmount)
                    .build());

            throwAmount = throwAmount - receiveAmount;
        }

        receiverRepository.save(Receiver.builder()
                .token(token)
                .receiveAmount(throwAmount)
                .build());

        return token;
    }

    public Long getReceiveMoneyInfo(Long receiveUserId, String roomId, String token) {

        // check token
        tokenInfoRedisRepository.findById(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN_VALUE));

        // get transaction
        return transactionRepository.findByToken(token)
                .map(transactionInfo -> {
                    // check receiver
                    if (receiveUserId.equals(transactionInfo.getSendUserId()) || !roomId.equals(transactionInfo.getRoomId())) {
                        throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED, "돈 받기를 할 수 없는 사용자 입니다.");
                    }

                    if (receiverRepository.findByTokenAndReceiveUserId(token, receiveUserId).isPresent()) {
                        throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED, "이미 돈 받기를한 사용자 입니다.");
                    }

                    // update receiver
                    return receiverRepository.findFirstByTokenAndReceiveUserIdIsNull(token)
                            .map(receiver -> {
                                receiver.setReceiveUserId(receiveUserId);
                                receiverRepository.save(receiver);
                                return receiver.getReceiveAmount();
                            })
                            .orElseThrow(() -> new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED, "더이상 받을 수 있는 돈이 없습니다."));
                })
                .orElseThrow(() -> new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED, "돈 받기를 할 수 없는 사용자 입니다."));
    }

    public Transactions getTransactions(Long sendUserId, String roomId, String token) {
        final LocalDateTime startDate = LocalDateTime.of(LocalDate.from(LocalDateTime.now().minusDays(6)), LocalTime.of(0, 0, 0));
        final LocalDateTime endDate = LocalDateTime.now();

        return transactionRepository.findByTokenAndSendUserIdAndThrowDateTimeBetween(token, sendUserId, startDate, endDate)
                .map(transaction -> Transactions.builder()
                        .throwDateTime(transaction.getThrowDateTime())
                        .throwAmount(transaction.getThrowAmount())
                        .receiveTotalAmount(receiverRepository.findReceiveTotalAmountByToken(token))
                        .receiversInfo(
                                receiverRepository.findByTokenAndReceiveUserIdIsNotNull(token).stream()
                                        .map(receiver -> ReceiverInfo.builder()
                                                .receiveUserId(receiver.getReceiveUserId())
                                                .receiveAmount(receiver.getReceiveAmount())
                                                .build())
                                        .collect(Collectors.toList())
                        )
                        .build())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "조회할 수 없는 정보입니다."));
    }

    // get token function
    String getToken() {
        final String token = generateToken();

        // check token && return token
        return tokenInfoRedisRepository.findById(token)
                .map(result -> getToken())
                .orElse(token);
    }

    // make token function
    static String generateToken() {
        StringBuilder token = new StringBuilder();

        for (int i = 0; i < TOKEN_SIZE; i++) {
            switch (random.nextInt(3)) {
                case 0:
                    token.append((char) (random.nextInt(26) + 97)); // a to z
                    break;
                case 1:
                    token.append((char) (random.nextInt(26) + 65)); // A to Z
                    break;
                case 2:
                    token.append(random.nextInt(10));      // 0 to 9
                    break;
            }
        }

        return token.toString();
    }

}
