package com.jindo.minipay.account.checking.controller;

import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
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

  @PostMapping("/charge")
  public ResponseEntity<Void> charge(@RequestBody @Valid CheckingAccountChargeRequest request) {
    checkingAccountService.charge(request);
    return ResponseEntity.ok().build();
  }
}
