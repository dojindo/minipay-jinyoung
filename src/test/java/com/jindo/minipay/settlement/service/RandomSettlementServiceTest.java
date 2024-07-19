package com.jindo.minipay.settlement.service;

import static com.jindo.minipay.settlement.constant.SettlementConstants.*;
import static com.jindo.minipay.settlement.type.SettlementType.RANDOM;
import static org.assertj.core.api.Assertions.assertThat;

import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.settlement.constant.SettlementConstants;
import com.jindo.minipay.settlement.dto.SettleDistributeRequest;
import com.jindo.minipay.settlement.dto.SettleDistributeResponse;
import com.jindo.minipay.settlement.repository.ParticipantSettlementRepository;
import com.jindo.minipay.settlement.repository.SettlementRepository;
import com.jindo.minipay.settlement.type.SettlementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RandomSettlementServiceTest {

  @Mock
  MemberRepository memberRepository;

  @Mock
  SettlementRepository settlementRepository;

  @Mock
  ParticipantSettlementRepository participantSettlementRepository;

  @InjectMocks
  RandomSettlementService settlementService;

  @Test
  @DisplayName("정산 타입을 리턴한다.")
  void getType() {
    SettlementType type = settlementService.getType();
    assertThat(type).isEqualTo(RANDOM);
  }

  @Nested
  @DisplayName("랜덤 정산 금액 분배")
  class RandomSettleDistributeMethod {

    Long requesterId = 1L;

    @Test
    @DisplayName("성공 - 총 정산금액을 랜덤으로 분배한다.")
    void distribute() {
      // given
      int participantsSize = 3;
      long totalAmount = 10_000L;

      SettleDistributeRequest request = new SettleDistributeRequest(
          requesterId, participantsSize, totalAmount, RANDOM);

      // when
      SettleDistributeResponse response = settlementService.distribute(request);

      // then
      assertThat(response.getSplitAmounts().length).isEqualTo(participantsSize);
      assertThat(response.getSupportsAmount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("성공 - 총 금액이 100으로 나누어 떨어지면, 분배금액또한 100으로 나누어떨어지는 수로 분배된다.")
    void distribute_mod_is_zero() {
      // given

      int participantsSize = 3;
      long totalAmount = 10_000L;

      SettleDistributeRequest request = new SettleDistributeRequest(
          requesterId, participantsSize, totalAmount, RANDOM);

      // when
      SettleDistributeResponse response = settlementService.distribute(request);

      long[] splitAmounts = response.getSplitAmounts();

      int correct = 0;
      for (long splitAmount : splitAmounts) {
        if (splitAmount % RANDOM_SETTLEMENT_UNIT == 0) {
          correct++;
        }
      }
      assertThat(correct).isEqualTo(splitAmounts.length);
    }

    @Test
    @DisplayName("성공 - 총 금액이 100으로 나누어 떨어지지 않으면, 하나의 분배금액를 제외한 나머지는 100으로 나누어 떨어진다.")
    void distribute_mod_is_non_zero() {
      // given

      int participantsSize = 3;
      long totalAmount = 9_999L;

      SettleDistributeRequest request = new SettleDistributeRequest(
          requesterId, participantsSize, totalAmount, RANDOM);

      // when
      SettleDistributeResponse response = settlementService.distribute(request);

      long[] splitAmounts = response.getSplitAmounts();

      int correct = 0;
      for (long splitAmount : splitAmounts) {
        if (splitAmount % RANDOM_SETTLEMENT_UNIT == 0) {
          correct++;
        }
      }
      assertThat(correct).isEqualTo(splitAmounts.length - 1);
    }
  }
}