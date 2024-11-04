package com.jindo.minipay.account.checking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.account.checking.dto.ChargeResponse;
import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
import com.jindo.minipay.account.checking.dto.CheckingAccountRemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.service.CheckingAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CheckingAccountController.class)
class CheckingAccountControllerTest {

  @MockBean
  CheckingAccountService checkingAccountService;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  static final String URI = "/account";

  static final String CHARGE_URL = "/charge";
  static final String REMIT_URL = "/remit";

  @Nested
  @DisplayName("메인 계좌 충전")
  class CheckingAccountChargeMethod {

    @Test
    @DisplayName("실패 - 회원아이디가 null 값인 경우")
    void charge_null_memberId() throws Exception {
      // given
      CheckingAccountChargeRequest request = new CheckingAccountChargeRequest(null, 10_000L);

      // when
      // then
      mockMvc.perform(post(URI + CHARGE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패 - 충전금액이 음수 값인 경우")
    void charge_negative_amount() throws Exception {
      // given
      CheckingAccountChargeRequest request = new CheckingAccountChargeRequest(1L, -10_000L);

      // when
      // then
      mockMvc.perform(post(URI + CHARGE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공")
    void charge() throws Exception {
      // given
      CheckingAccountChargeRequest request = new CheckingAccountChargeRequest(1L, 10_000L);
      ChargeResponse response = new ChargeResponse(10_000L);

      when(checkingAccountService.charge(any())).thenReturn(response);

      // when
      // then
      mockMvc.perform(post(URI + CHARGE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.balance").value(10_000L));
    }
  }

  @Nested
  @DisplayName("친구에게 송금")
  class CheckingAccountWireMethod {

    @Test
    @DisplayName("실패 - 송신자 아이디가 null 값인 경우")
    void wire_null_senderId() throws Exception {
      // given
      CheckingAccountRemitRequest request = new CheckingAccountRemitRequest(null, 1L, 10_000L);

      // when
      // then
      mockMvc.perform(post(URI + REMIT_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패 - 수신자 아이디가 null 값인 경우")
    void wire_null_receiverId() throws Exception {
      // given
      CheckingAccountRemitRequest request = new CheckingAccountRemitRequest(1L, null, 10_000L);

      // when
      // then
      mockMvc.perform(post(URI + REMIT_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패 - 송금금액이 0인 경우")
    void wire_zero_amount() throws Exception {
      // given
      CheckingAccountRemitRequest request = new CheckingAccountRemitRequest(1L, 1L, 0);

      // when
      // then
      mockMvc.perform(post(URI + REMIT_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패 - 송금금액이 음수 값인 경우")
    void wire_negative_amount() throws Exception {
      // given
      CheckingAccountRemitRequest request = new CheckingAccountRemitRequest(1L, 1L, -10_000L);

      // when
      // then
      mockMvc.perform(post(URI + REMIT_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공")
    void wire() throws Exception {
      // given
      CheckingAccountRemitRequest request = new CheckingAccountRemitRequest(1L, 1L, 10_000L);
      RemitResponse response = new RemitResponse(10_000L);

      when(checkingAccountService.remit(any())).thenReturn(response);

      // when
      // then
      mockMvc.perform(post(URI + REMIT_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.balance").value(10_000L));
    }
  }
}