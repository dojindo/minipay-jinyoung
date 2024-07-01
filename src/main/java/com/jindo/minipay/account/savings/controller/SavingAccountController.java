package com.jindo.minipay.account.savings.controller;

import com.jindo.minipay.account.savings.dto.SavingAccountCreateRequest;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateResponse;
import com.jindo.minipay.account.savings.dto.SavingAccountDepositRequest;
import com.jindo.minipay.account.savings.dto.SavingAccountDepositResponse;
import com.jindo.minipay.account.savings.service.SavingAccountService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saving")
public class SavingAccountController {

  private final SavingAccountService savingAccountService;

  // TODO : @AuthenticationPrincipal or @Login 사용
  @PostMapping
  public ResponseEntity<SavingAccountCreateResponse> create(
      @RequestBody @Valid SavingAccountCreateRequest request) {
    SavingAccountCreateResponse response = savingAccountService.create(request);
    return ResponseEntity.created(URI.create("/saving/" + response.getSavingAccountId()))
        .body(response);
  }

  @PostMapping("/deposit")
  public ResponseEntity<SavingAccountDepositResponse> deposit(
      @RequestBody @Valid SavingAccountDepositRequest request) {
    return ResponseEntity.ok(savingAccountService.deposit(request));
  }
}
