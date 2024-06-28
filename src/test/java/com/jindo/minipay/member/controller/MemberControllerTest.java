package com.jindo.minipay.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jindo.minipay.member.dto.MemberSignupRequest;
import com.jindo.minipay.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

  @MockBean
  MemberService memberService;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  static final String URI = "/member";

  @Nested
  @DisplayName("회원 등록")
  class MemberSignupMethod {

    @Test
    @DisplayName("실패 - 유저네임이 빈 값인 경우")
    void signup_empty_username() throws Exception {
      // given
      MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
          .username("")
          .password("1q2w3e4r!")
          .build();

      // when
      // then
      mockMvc.perform(post(URI)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(memberSignupRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패 - 패스워드가 빈 값인 경우")
    void signup_empty_password() throws Exception {
      // given
      MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
          .username("username1")
          .password("")
          .build();

      // when
      // then
      mockMvc.perform(post(URI)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(memberSignupRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공")
    void signup() throws Exception {
      // given
      MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
          .username("username1")
          .password("1q2w3e4r!")
          .build();

      // when
      // then
      mockMvc.perform(post(URI)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(memberSignupRequest)))
          .andExpect(status().isCreated());
    }
  }
}