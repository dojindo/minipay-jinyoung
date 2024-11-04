package com.jindo.minipay.account.checking.repository.redis;

import com.jindo.minipay.account.checking.entity.ChargeLimit;
import com.jindo.minipay.account.common.constant.AccountConstants;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChargeLimitRepository {

  private final RedisTemplate<String, Object> redisTemplate;

  public void save(Long memberId, long amount, Duration ttl) {
    ChargeLimit chargeLimit = new ChargeLimit(memberId, amount);
    String key = AccountConstants.CHARGE_AMOUNT_KEY + memberId;
    redisTemplate.opsForValue().set(key, chargeLimit, ttl);
  }

  public Optional<ChargeLimit> findByMemberId(Long memberId) {
    return Optional.ofNullable((ChargeLimit) redisTemplate.opsForValue()
        .get(AccountConstants.CHARGE_AMOUNT_KEY + memberId));
  }
}
