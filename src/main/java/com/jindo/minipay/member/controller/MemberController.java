package com.jindo.minipay.member.controller;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;

import com.jindo.minipay.member.dto.MemberSignupRequest;
import com.jindo.minipay.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

  private final MemberService memberService;

  @PostMapping
  public ResponseEntity<?> signup(@RequestBody @Valid MemberSignupRequest memberSignupRequest) {
    memberService.signup(memberSignupRequest);
    return ResponseEntity.status(SC_CREATED).build();
  }
}
