package com.jindo.minipay.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSignupDto {

  @NotBlank(message = "아이디를 입력해주세요.")
  private String username;

  @NotBlank(message = "비밀번호를 입력해주세요.")
  private String password;

}
