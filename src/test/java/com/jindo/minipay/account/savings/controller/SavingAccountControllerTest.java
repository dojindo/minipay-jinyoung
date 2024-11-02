package com.jindo.minipay.account.savings.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateRequest;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateResponse;
import com.jindo.minipay.account.savings.dto.SavingAccountDepositRequest;
import com.jindo.minipay.account.savings.dto.SavingAccountDepositResponse;
import com.jindo.minipay.account.savings.service.SavingAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SavingAccountController.class)
class SavingAccountControllerTest {

  @MockBean
  SavingAccountService savingAccountService;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  static final String URI = "/saving";

  @Nested
  @DisplayName("적금 계좌 등록")
  class SavingAccountCreateMethod {

    @Test
    @DisplayName("실패 - 회원ID가 null인 경우")
    void create_null_memberId() throws Exception {
      // given
      SavingAccountCreateRequest request = new SavingAccountCreateRequest(null);
      SavingAccountCreateResponse response = new SavingAccountCreateResponse(1L);
      // when
      when(savingAccountService.create(any())).thenReturn(response);
      // then
      mockMvc.perform(post(URI)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공")
    void create() throws Exception {
      // given
      SavingAccountCreateRequest request = new SavingAccountCreateRequest(1L);
      SavingAccountCreateResponse response = new SavingAccountCreateResponse(1L);
      given(savingAccountService.create(any())).willReturn(response);

      // when
      // then
      mockMvc.perform(post(URI)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated()).andDo(print());
    }
  }

  @Nested
  @DisplayName("적금 계좌 입금")
  class SavingAccountDepositMethod {

    @Test
    @DisplayName("실패 - ownerId가 null인 경우")
    void deposit_null_ownerId() throws Exception {
      // given
      SavingAccountDepositRequest request = new SavingAccountDepositRequest(null, 1L, 10_000L);

      // when
      // then
      mockMvc.perform(post(URI + "/deposit")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패 - savingAccountId가 null인 경우")
    void deposit_null_savingAccountId() throws Exception {
      // given
      SavingAccountDepositRequest request = new SavingAccountDepositRequest(1L, null, 10_000L);

      // when
      // then
      mockMvc.perform(post(URI + "/deposit")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패 - 입금금액이 음수인 경우")
    void deposit_negative_amount() throws Exception {
      // given
      SavingAccountDepositRequest request = new SavingAccountDepositRequest(1L, 1L, -10_000L);

      // when
      // then
      mockMvc.perform(post(URI + "/deposit")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공")
    void deposit() throws Exception {
      // given
      SavingAccountDepositRequest request = new SavingAccountDepositRequest(1L, 1L, 10_000L);

      SavingAccountDepositResponse response = new SavingAccountDepositResponse(10_000L);

      // when
      when(savingAccountService.deposit(any())).thenReturn(response);
      // then
      mockMvc.perform(post(URI + "/deposit")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }
  }
}