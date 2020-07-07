package com.random.money.repository;

import com.random.money.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByToken(String token);
    Optional<Transaction> findByTokenAndSendUserIdAndThrowDateTimeBetween(String token, Long sendUserId, LocalDateTime startDate, LocalDateTime endDate);

}
