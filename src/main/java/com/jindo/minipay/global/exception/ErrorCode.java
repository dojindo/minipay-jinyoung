package com.jindo.minipay.global.exception;

import static jakarta.servlet.http.HttpServletResponse.SC_CONFLICT;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  ALREADY_EXISTS_USERNAME(SC_CONFLICT, "중복된 ID 입니다.");;

  private final int code;

  private final String message;
}
