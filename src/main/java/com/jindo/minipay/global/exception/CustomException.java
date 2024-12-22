package com.jindo.minipay.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final int statusCode;

  public CustomException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    statusCode = errorCode.getCode();
  }

  public CustomException(ErrorCode errorCode, Throwable throwable) {
    super(errorCode.getMessage(), throwable);
    statusCode = errorCode.getCode();
  }
}
