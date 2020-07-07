package com.random.money.repository;

import com.random.money.model.TokenInfo;
import org.springframework.data.repository.CrudRepository;

public interface TokenInfoRedisRepository extends CrudRepository<TokenInfo, String> {
}
