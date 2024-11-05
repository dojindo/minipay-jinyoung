package com.jindo.minipay.account.checking.controller;

import com.jindo.minipay.account.checking.dto.ChargeResponse;
import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
import com.jindo.minipay.account.checking.dto.CheckingAccountRemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.service.CheckingAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class CheckingAccountController {

  private final CheckingAccountService checkingAccountService;

  /**
   * 메인 계좌 충전
   */
  @PostMapping("/charge")
  public ResponseEntity<ChargeResponse> charge(
      @RequestBody @Valid CheckingAccountChargeRequest request) {
    return ResponseEntity.ok().body(checkingAccountService.charge(request));
  }

  /**
   * 메인 계좌 간 송금
   */
  @PostMapping("/remit")
  public ResponseEntity<RemitResponse> remit(
      @RequestBody @Valid CheckingAccountRemitRequest request) {
    return ResponseEntity.ok().body(checkingAccountService.remit(request));
  }
}