package com.jindo.minipay.settlement.controller;

import static com.jindo.minipay.global.exception.ErrorCode.INSUFFICIENT_SETTLE_AMOUNT;
import static com.jindo.minipay.global.exception.ErrorCode.INVALID_SETTLE_REQUEST;
import static com.jindo.minipay.settlement.constant.SettlementConstants.RANDOM_SETTLEMENT_MIN_AMOUNT;
import static com.jindo.minipay.settlement.constant.SettlementConstants.SETTLEMENT_DISTRIBUTE_MAX_AMOUNT;
import static com.jindo.minipay.settlement.constant.SettlementConstants.SETTLEMENT_DISTRIBUTE_MIN_AMOUNT;
import static com.jindo.minipay.settlement.constant.SettlementConstants.SETTLEMENT_PARTICIPANTS_MAX_SIZE;
import static com.jindo.minipay.settlement.constant.SettlementConstants.SETTLEMENT_PARTICIPANTS_MIN_SIZE;
import static com.jindo.minipay.settlement.type.SettlementType.EQUAL;
import static com.jindo.minipay.settlement.type.SettlementType.RANDOM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.settlement.dto.SettleDistributeRequest;
import com.jindo.minipay.settlement.dto.SettleDistributeResponse;
import com.jindo.minipay.settlement.dto.SettleRequest;
import com.jindo.minipay.settlement.service.EqualSettlementService;
import com.jindo.minipay.settlement.service.RandomSettlementService;
import com.jindo.minipay.settlement.service.SettlementServiceFinder;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

  @MockBean
  SettlementServiceFinder settlementServiceFinder;

  @MockBean
  EqualSettlementService equalSettlementService;

  @MockBean
  RandomSettlementService randomSettlementService;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  static final String URI = "/settle";

  @Nested
  @DisplayName("정산 분배금액 계산")
  class SettleDistributeMethod {

    @Test
    @DisplayName("실패 - 요청자 ID가 null인 경우")
    void distribute_requesterId_null() throws Exception {
      // given
      SettleDistributeRequest request = new SettleDistributeRequest(
          null, SETTLEMENT_PARTICIPANTS_MIN_SIZE, 10_000L, EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI + "/distribute")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 최소 참여자 수보다 적은 요청인 경우")
    void distribute_participants_size_smaller_than_min() throws Exception {
      // given
      SettleDistributeRequest request = new SettleDistributeRequest(
          1L, SETTLEMENT_PARTICIPANTS_MIN_SIZE - 1, 10_000L, EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI + "/distribute")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 최대 참여자 수보다 많은 요청인 경우")
    void distribute_participants_size_bigger_than_max() throws Exception {
      // given
      SettleDistributeRequest request = new SettleDistributeRequest(
          1L, SETTLEMENT_PARTICIPANTS_MAX_SIZE + 1, 10_000L, EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI + "/distribute")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 정산 금액이 최소 요구사항보다 적은 경우")
    void distribute_total_amount_smaller_than_min() throws Exception {
      // given
      SettleDistributeRequest request = new SettleDistributeRequest(
          1L, SETTLEMENT_PARTICIPANTS_MIN_SIZE, SETTLEMENT_DISTRIBUTE_MIN_AMOUNT - 1,
          EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI + "/distribute")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 정산 금액이 최대 요구사항보다 많은 경우")
    void distribute_total_amount_bigger_than_max() throws Exception {
      // given
      SettleDistributeRequest request = new SettleDistributeRequest(
          1L, SETTLEMENT_PARTICIPANTS_MIN_SIZE, SETTLEMENT_DISTRIBUTE_MAX_AMOUNT + 1,
          EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI + "/distribute")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 정산 타입이 null인 경우")
    void distribute_settle_type_null() throws Exception {
      // given
      SettleDistributeRequest request = new SettleDistributeRequest(
          1L, SETTLEMENT_PARTICIPANTS_MIN_SIZE, SETTLEMENT_DISTRIBUTE_MIN_AMOUNT, null);
      // when
      // then
      mockMvc.perform(
              post(URI + "/distribute")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("성공 - 1/N 정산 금액 분배")
    void distribute_of_equal_settle() throws Exception {
      // given
      SettleDistributeRequest request = new SettleDistributeRequest(
          1L, SETTLEMENT_PARTICIPANTS_MIN_SIZE, SETTLEMENT_DISTRIBUTE_MAX_AMOUNT,
          EQUAL);

      long splitAmount = request.getTotalAmount() / request.getParticipantsSize();
      long supportsAmount = request.getTotalAmount() % request.getParticipantsSize();
      SettleDistributeResponse response = SettleDistributeResponse.ofEqualSettle(
          request.getParticipantsSize(), splitAmount, supportsAmount);

      // when
      when(settlementServiceFinder.find(request.getSettlementType())).thenReturn(
          equalSettlementService);
      when(equalSettlementService.distribute(any())).thenReturn(response);

      // then
      mockMvc.perform(
              post(URI + "/distribute")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isOk())
          .andDo(print());
    }

    @Test
    @DisplayName("성공 - 랜덤 정산 금액 분배")
    void distribute_of_random_settle() throws Exception {
      // given
      SettleDistributeRequest request = new SettleDistributeRequest(
          1L, SETTLEMENT_PARTICIPANTS_MIN_SIZE, SETTLEMENT_DISTRIBUTE_MAX_AMOUNT,
          RANDOM);

      long[] splitAmounts = new long[request.getParticipantsSize()];
      SettleDistributeResponse response = SettleDistributeResponse.ofRandomSettle(splitAmounts);

      // when
      when(settlementServiceFinder.find(request.getSettlementType())).thenReturn(
          randomSettlementService);
      when(equalSettlementService.distribute(any())).thenReturn(response);

      // then
      mockMvc.perform(
              post(URI + "/distribute")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isOk())
          .andDo(print());
    }
  }

  @Nested
  @DisplayName("정산 요청")
  class SettleMethod {

    Long requesterId = 1L;
    List<Long> participantsIds = List.of(1L, 2L);
    long totalAmount = 1_000L;
    long supportsAmount = 0;
    long[] amounts = new long[]{500L, 500L};

    @Test
    @DisplayName("실패 - 요청자 ID가 null인 경우")
    void settle_requesterId_null() throws Exception {
      // given
      SettleRequest request = new SettleRequest(
          null, participantsIds, totalAmount, supportsAmount, amounts, EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 참여자 수가 최소 요구사항보다 적은 경우")
    void settle_participants_size_smaller_than_min() throws Exception {
      // given
      SettleRequest request = new SettleRequest(
          requesterId, List.of(1L), totalAmount, supportsAmount, amounts, EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 참여자 수가 최대 요구사항보다 큰 경우")
    void settle_participants_size_bigger_than_max() throws Exception {
      List<Long> oversizeParticipantIds = new ArrayList<>();
      for (int i = 1; i <= SETTLEMENT_PARTICIPANTS_MAX_SIZE + 1; i++) {
        oversizeParticipantIds.add((long) i);
      }
      // given
      SettleRequest request = new SettleRequest(
          requesterId, oversizeParticipantIds, totalAmount, supportsAmount, amounts, EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 분배 금액 배열의 길이가 최소 요구사항보다 짧은 경우")
    void settle_amounts_length_shorter_than_min() throws Exception {
      // given
      long[] shortAmounts = new long[SETTLEMENT_PARTICIPANTS_MIN_SIZE - 1];

      SettleRequest request = new SettleRequest(
          requesterId, participantsIds, totalAmount, supportsAmount, shortAmounts, EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 분배 금액 배열의 길이가 최대 요구사항보다 긴 경우")
    void settle_amounts_length_longer_than_min() throws Exception {
      // given
      long[] longAmounts = new long[SETTLEMENT_PARTICIPANTS_MAX_SIZE + 1];

      SettleRequest request = new SettleRequest(
          requesterId, participantsIds, totalAmount, supportsAmount, longAmounts, EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 정산 요청 금액이 최소 참여자 수보다 작은 경우")
    void settle_total_amount_smaller_than_participants_min_size() throws Exception {
      // given
      long smallTotalAmount = SETTLEMENT_PARTICIPANTS_MIN_SIZE - 1;

      SettleRequest request = new SettleRequest(
          requesterId, participantsIds, smallTotalAmount, supportsAmount, amounts, EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 정산 타입이 null인 경우")
    void settle_settle_type_is_null() throws Exception {
      // given
      SettleRequest request = new SettleRequest(
          requesterId, participantsIds, totalAmount, supportsAmount, amounts, null);
      // when
      // then
      mockMvc.perform(
              post(URI)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 참여자 수와 amounts 배열의 길이가 다를 경우")
    void settle_participants_size_and_amounts_length_is_not_same() throws Exception {
      // given
      List<Long> twoParticipantIds = List.of(1L, 2L);
      long[] threeLengthAmounts = new long[3];

      SettleRequest request = new SettleRequest(
          requesterId, twoParticipantIds, totalAmount, supportsAmount, threeLengthAmounts, EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(INVALID_SETTLE_REQUEST.getMessage()))
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 참여자 수가 총 정산 금액보다 클 경우")
    void settle_participants_size_bigger_than_total_amount() throws Exception {
      // given
      List<Long> fiveParticipantsIds = List.of(1L, 2L, 3L, 4L, 5L);
      long[] amounts = new long[fiveParticipantsIds.size()];
      long smallerTotalAmount = 4L;

      SettleRequest request = new SettleRequest(
          requesterId, fiveParticipantsIds, smallerTotalAmount, supportsAmount, amounts, EQUAL);
      // when
      // then
      mockMvc.perform(
              post(URI)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(INSUFFICIENT_SETTLE_AMOUNT.getMessage()))
          .andDo(print());
    }

    @Test
    @DisplayName("실패 - 랜덤 정산 시, 총 정산 금액이 랜덤 정산의 최소 요구 금액보다 적은 경우")
    void random_settle_total_amount_smaller_than_random_settlement_min_amount() throws Exception {
      // given
      long totalAmount = RANDOM_SETTLEMENT_MIN_AMOUNT - 1;

      SettleRequest request = new SettleRequest(
          requesterId, participantsIds, totalAmount, supportsAmount, amounts, RANDOM);
      // when
      // then
      mockMvc.perform(
              post(URI)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(INSUFFICIENT_SETTLE_AMOUNT.getMessage()))
          .andDo(print());
    }

    @Test
    @DisplayName("성공 - 1/N 정산")
    void settle_of_equal() throws Exception {
      // given
      SettleRequest request = new SettleRequest(
          requesterId, participantsIds, totalAmount, supportsAmount, amounts, EQUAL);

      Long response = 1L;

      // when
      when(settlementServiceFinder.find(request.getSettlementType())).thenReturn(
          equalSettlementService);
      when(equalSettlementService.settle(any())).thenReturn(response);

      // then
      mockMvc.perform(
              post(URI)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isCreated())
          .andExpect(header().string("Location", URI + "/" + response))
          .andDo(print());
    }

    @Test
    @DisplayName("성공 - 랜덤 정산")
    void settle_of_random() throws Exception {
      // given
      SettleRequest request = new SettleRequest(
          requesterId, participantsIds, totalAmount, supportsAmount, amounts, RANDOM);

      Long response = 1L;

      // when
      when(settlementServiceFinder.find(request.getSettlementType())).thenReturn(
          randomSettlementService);
      when(randomSettlementService.settle(any())).thenReturn(response);

      // then
      mockMvc.perform(
              post(URI)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          ).andExpect(status().isCreated())
          .andExpect(header().string("Location", URI + "/" + response))
          .andDo(print());
    }
  }
}