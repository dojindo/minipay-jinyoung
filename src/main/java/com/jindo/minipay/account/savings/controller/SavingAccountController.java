package com.jindo.minipay.account.savings.controller;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;

import com.jindo.minipay.account.savings.dto.SavingAccountCreateRequest;
import com.jindo.minipay.account.savings.dto.SavingAccountDepositRequest;
import com.jindo.minipay.account.savings.service.SavingAccountService;
import jakarta.validation.Valid;
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
  public ResponseEntity<?> create(@RequestBody @Valid SavingAccountCreateRequest request) {
    return ResponseEntity.status(SC_CREATED).body(savingAccountService.create(request));
  }

  @PostMapping("/deposit")
  public ResponseEntity<?> deposit(@RequestBody @Valid SavingAccountDepositRequest request) {
    return ResponseEntity.ok(savingAccountService.deposit(request));
  }
}
