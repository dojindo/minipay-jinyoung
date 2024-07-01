package com.jindo.minipay.account.checking.repository.redis;

import com.jindo.minipay.account.checking.entity.ChargeAmount;
import com.jindo.minipay.account.common.constant.AccountConstants;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChargeAmountRepository {

  private final RedisTemplate<String, Object> redisTemplate;

  public void save(Long memberId, long amount, Duration ttl) {
    ChargeAmount chargeAmount = new ChargeAmount(memberId, amount);
    String key = AccountConstants.CHARGE_AMOUNT_KEY + memberId;
    redisTemplate.opsForValue().set(key, chargeAmount, ttl);
  }

  public Optional<ChargeAmount> findByMemberId(Long memberId) {
    return Optional.ofNullable((ChargeAmount) redisTemplate.opsForValue()
        .get(AccountConstants.CHARGE_AMOUNT_KEY + memberId));
  }
}
