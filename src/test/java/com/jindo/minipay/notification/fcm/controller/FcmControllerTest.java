package com.jindo.minipay.notification.fcm.controller;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.notification.fcm.dto.FcmTokenRegisterRequest;
import com.jindo.minipay.notification.fcm.service.FcmService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FcmController.class)
class FcmControllerTest {

  @MockBean
  FcmService fcmService;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  @DisplayName("성공 - FCM 토큰 등록")
  void register_fcm_token() throws Exception {
    // given
    String tokenValue = "request token value";
    FcmTokenRegisterRequest request = new FcmTokenRegisterRequest(1L, tokenValue);

    // when
    doNothing().when(fcmService).registerToken(request);

    // then
    mockMvc.perform(post("/fcm/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }
}