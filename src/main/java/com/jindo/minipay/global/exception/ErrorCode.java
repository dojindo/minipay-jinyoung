package com.jindo.minipay.global.exception;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_CONFLICT;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  ALREADY_EXISTS_USERNAME(SC_CONFLICT, "중복된 ID 입니다."),
  ACCOUNT_NOT_FOUND(SC_NOT_FOUND, "존재하지 않는 계좌입니다."),
  MEMBER_NOT_FOUND(SC_NOT_FOUND, "존재하지 않는 회원입니다."),
  CHARGE_LIMIT_EXCEEDED(SC_BAD_REQUEST, "금일 충전 한도를 초과하였습니다."),
  BALANCE_NOT_ENOUGH(SC_BAD_REQUEST, "메인 계좌에 잔액이 부족합니다.");

  private final int code;

  private final String message;
}
