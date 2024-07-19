package com.jindo.minipay.settlement.service;

import static com.jindo.minipay.global.exception.ErrorCode.INVALID_PARTICIPANT;
import static com.jindo.minipay.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.jindo.minipay.settlement.type.SettlementType.EQUAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.settlement.dto.SettleDistributeRequest;
import com.jindo.minipay.settlement.dto.SettleDistributeResponse;
import com.jindo.minipay.settlement.dto.SettleRequest;
import com.jindo.minipay.settlement.entity.Settlement;
import com.jindo.minipay.settlement.repository.ParticipantSettlementRepository;
import com.jindo.minipay.settlement.repository.SettlementRepository;
import com.jindo.minipay.settlement.type.SettlementType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EqualSettlementServiceTest {

  @Mock
  MemberRepository memberRepository;

  @Mock
  SettlementRepository settlementRepository;

  @Mock
  ParticipantSettlementRepository participantSettlementRepository;

  @InjectMocks
  EqualSettlementService settlementService;

  @Test
  @DisplayName("정산 타입을 리턴한다.")
  void getType() {
    SettlementType type = settlementService.getType();
    assertThat(type).isEqualTo(EQUAL);
  }

  @Nested
  @DisplayName("1/N 정산 금액 분배")
  class EqualSettleDistributeMethod {

    Long requesterId = 1L;

    @Test
    @DisplayName("성공 - 총 정산금액을 참여자 수로 나눈 몫으로 분배한다.")
    void distribute() {
      // given
      int participantsSize = 3;
      long totalAmount = 10_000L;

      long splitAmount = totalAmount / participantsSize;
      long[] amounts = new long[]{splitAmount, splitAmount, splitAmount};

      SettleDistributeRequest request = new SettleDistributeRequest(
          requesterId, participantsSize, totalAmount, EQUAL);

      // when
      SettleDistributeResponse response = settlementService.distribute(request);

      // then
      assertThat(response.getSplitAmounts()).isEqualTo(amounts);
    }

    @Test
    @DisplayName("성공 - 총 정산금액이 참여자 수로 나누어 떨어지면 지원해주지 않는다.")
    void distribute_mod_is_zero() {
      // given
      int participantsSize = 3;
      long totalAmount = 9_999L;

      SettleDistributeRequest request = new SettleDistributeRequest(
          requesterId, participantsSize, totalAmount, EQUAL);

      // when
      SettleDistributeResponse response = settlementService.distribute(request);

      // then
      assertThat(response.getSupportsAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("성공 - 총 정산금액이 참여자 수로 나누어 떨어지지 않으면 부족한 만큼 지원해준다.")
    void distribute_mod_is_non_zero() {
      // given
      int participantsSize = 3;
      long totalAmount = 10_000L;

      SettleDistributeRequest request = new SettleDistributeRequest(
          requesterId, participantsSize, totalAmount, EQUAL);

      // when
      SettleDistributeResponse response = settlementService.distribute(request);

      // then
      assertThat(response.getSupportsAmount()).isEqualTo(1L);
    }
  }

  @Nested
  @DisplayName("1/N 정산 요청")
  class EqualSettleMethod {

    Long requesterId = 1L;
    List<Long> participantsIds = List.of(1L, 2L, 3L);
    long totalAmount = 10_000L;
    long supportsAmount = 1L;
    long[] amounts = new long[]{3_333L, 3_333L, 3_333L};

    SettleRequest request = new SettleRequest(
        requesterId, participantsIds, totalAmount, supportsAmount, amounts, EQUAL);

    @Test
    @DisplayName("실패 - 존재히지 않는 참여자인 경우")
    void settle_not_exists_participant() {
      // given
      Member participant1 = Member.builder()
          .id(1L)
          .build();

      Member participant2 = Member.builder()
          .id(2L)
          .build();

      given(memberRepository.findByIdIn(request.getParticipantIds()))
          .willReturn(List.of(participant1, participant2));

      // when
      // then
      assertThatThrownBy(() -> settlementService.settle(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(INVALID_PARTICIPANT.getMessage());
    }

    @Test
    @DisplayName("실패 - 존재히지 않는 요청자인 경우")
    void settle_not_exists_requester() {
      // given
      Member participant1 = Member.builder()
          .id(1L)
          .build();

      Member participant2 = Member.builder()
          .id(2L)
          .build();

      Member participant3 = Member.builder()
          .id(2L)
          .build();

      given(memberRepository.findByIdIn(request.getParticipantIds()))
          .willReturn(List.of(participant1, participant2, participant3));

      given(memberRepository.findById(request.getRequesterId())).willThrow(
          new CustomException(MEMBER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> settlementService.settle(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("성공")
    void settle() {
      // given
      Member requester = Member.builder()
          .id(1L)
          .build();

      Member participant1 = Member.builder()
          .id(1L)
          .build();

      Member participant2 = Member.builder()
          .id(2L)
          .build();

      Member participant3 = Member.builder()
          .id(2L)
          .build();

      Settlement savedSettlement = Settlement.builder()
          .requester(requester)
          .totalAmount(request.getTotalAmount())
          .supportsAmount(request.getSupportsAmount())
          .settlementType(request.getSettlementType())
          .build();

      given(memberRepository.findByIdIn(request.getParticipantIds()))
          .willReturn(List.of(participant1, participant2, participant3));

      given(memberRepository.findById(request.getRequesterId()))
          .willReturn(Optional.of(requester));

      given(settlementRepository.save(any())).willReturn(savedSettlement);

      // when
      Long settleId = settlementService.settle(request);

      // then
      verify(settlementRepository).save(any());
      verify(participantSettlementRepository, times(request.getAmounts().length)).save(any());
      assertThat(settleId).isEqualTo(savedSettlement.getId());
    }
  }
}