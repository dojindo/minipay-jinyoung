package com.jindo.minipay.global.exception;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> customExceptionHandler(CustomException e) {
    ErrorResponse errorResponse = new ErrorResponse(e.getStatusCode(), e.getMessage());
    return ResponseEntity.status(errorResponse.getCode()).body(errorResponse);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> invalidRequestHandler(MethodArgumentNotValidException e) {
    ErrorResponse errorResponse = new ErrorResponse(SC_BAD_REQUEST, e.getMessage());
    // TODO : 필드 별 에러메세지 응답하기
    return ResponseEntity.status(errorResponse.getCode()).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> exceptionHandler(Exception e) {
    ErrorResponse errorResponse = new ErrorResponse(SC_INTERNAL_SERVER_ERROR, e.getMessage());
    return ResponseEntity.status(errorResponse.getCode()).body(errorResponse);
  }
}
