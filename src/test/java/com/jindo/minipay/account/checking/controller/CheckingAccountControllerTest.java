package com.jindo.minipay.account.checking.controller;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.account.checking.dto.AccountChargeRequest;
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

  @Nested
  @DisplayName("메인 계좌 충전")
  class CheckingAccountChargeMethod {

    @Test
    @DisplayName("실패 - 회원아이디가 null 값인 경우")
    void charge_null_memberId() throws Exception {
      // given
      AccountChargeRequest request = new AccountChargeRequest(null, 10_000L);

      // when
      doNothing().when(checkingAccountService).charge(request);
      // then
      mockMvc.perform(post(URI + "/charge")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패 - 충전금액이 음수 값인 경우")
    void charge_null_amount() throws Exception {
      // given
      AccountChargeRequest request = new AccountChargeRequest(1L, -10_000L);

      // when
      doNothing().when(checkingAccountService).charge(request);
      // then
      mockMvc.perform(post(URI + "/charge")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공")
    void charge() throws Exception {
      // given
      AccountChargeRequest request = new AccountChargeRequest(1L, 10_000L);

      // when
      doNothing().when(checkingAccountService).charge(request);
      // then
      mockMvc.perform(post(URI + "/charge")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }
  }
}