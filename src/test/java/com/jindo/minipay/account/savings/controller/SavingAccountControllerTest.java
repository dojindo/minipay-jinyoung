package com.jindo.minipay.account.savings.controller;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateRequest;
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

      // when
      doNothing().when(savingAccountService).create(request);
      // then
      mockMvc.perform(post(URI)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공")
    void charge() throws Exception {
      // given
      SavingAccountCreateRequest request = new SavingAccountCreateRequest(1L);

      // when
      doNothing().when(savingAccountService).create(request);
      // then
      mockMvc.perform(post(URI)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());
    }
  }
}