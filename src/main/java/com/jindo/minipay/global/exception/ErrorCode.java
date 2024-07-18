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

  INVALID_PARTICIPANT(SC_NOT_FOUND, "유효하지 않은 참여자입니다."),

  CHARGE_LIMIT_EXCEEDED(SC_BAD_REQUEST, "금일 충전 한도를 초과하였습니다."),
  NOT_EXISTS_SETTLE_TYPE(SC_BAD_REQUEST, "존재하지 않는 정산 방법입니다."),
  BALANCE_NOT_ENOUGH(SC_BAD_REQUEST, "메인 계좌에 잔액이 부족합니다."),
  INSUFFICIENT_SETTLE_AMOUNT(SC_BAD_REQUEST, "정산 금액이 너무 적습니다."),
  INVALID_SETTLE_REQUEST(SC_BAD_REQUEST, "유효하지 않은 요청입니다.");


  private final int code;

  private final String message;
}
