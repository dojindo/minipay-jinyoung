package com.jindo.minipay.settlement.service;

import static com.jindo.minipay.settlement.type.SettlementType.EQUAL;
import static com.jindo.minipay.settlement.type.SettlementType.RANDOM;
import static com.jindo.minipay.global.exception.ErrorCode.NOT_EXISTS_SETTLE_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jindo.minipay.settlement.type.SettlementType;
import com.jindo.minipay.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SettlementServiceFinderTest {

  @Autowired
  SettlementServiceFinder settlementServiceFinder;

  @Autowired
  EqualSettlementService equalSettlementService;

  @Autowired
  RandomSettlementService randomSettlementService;

  @Nested
  @DisplayName("SettlementType에 따른 Service 찾기")
  class FindSettleService {

    @Test
    @DisplayName("실패 - 타입이 유효하지 않은 경우 예외를 던진다.")
    void if_settlement_type_is_invalid() {
      // given
      SettlementType settlementType = null;

      // when
      // then
      assertThatThrownBy(() -> settlementServiceFinder.find(settlementType))
          .isInstanceOf(CustomException.class)
          .hasMessage(NOT_EXISTS_SETTLE_TYPE.getMessage());
    }

    @Test
    @DisplayName("성공 - 타입이 EQUAL이면 EqualSettlementService를 찾는다.")
    void if_settlement_type_is_equal_find_EqualSettlementService() {
      // given
      // when
      SettlementService settlementService = settlementServiceFinder.find(EQUAL);

      // then
      assertThat(settlementService).isEqualTo(equalSettlementService);
    }

    @Test
    @DisplayName("성공 - 타입이 RANDOM이면 RandomSettlementService를 찾는다.")
    void if_settlement_type_is_equal_find_RandomSettlementService() {
      // given
      // when
      SettlementService settlementService = settlementServiceFinder.find(RANDOM);

      // then
      assertThat(settlementService).isEqualTo(randomSettlementService);
    }
  }
}