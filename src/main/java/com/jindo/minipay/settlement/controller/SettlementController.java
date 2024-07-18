package com.jindo.minipay.settlement.controller;

import com.jindo.minipay.settlement.dto.SettleDistributeRequest;
import com.jindo.minipay.settlement.dto.SettleDistributeResponse;
import com.jindo.minipay.settlement.service.SettlementServiceFinder;
import com.jindo.minipay.settlement.dto.SettleRequest;
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
@RequestMapping("/settle")
public class SettlementController {

  private final SettlementServiceFinder settlementServiceFinder;

  /**
   * 정산 분배금액 계산
   */
  @PostMapping("/distribute")
  public ResponseEntity<SettleDistributeResponse> distribute(@RequestBody @Valid SettleDistributeRequest request) {
    return ResponseEntity.ok(settlementServiceFinder.find(request.getSettlementType()).distribute(request));
  }

  /**
   * 정산 요청
   */
  @PostMapping
  public ResponseEntity<Void> settle(@RequestBody @Valid SettleRequest request) {
    request.additionalValidate();
    Long settleId = settlementServiceFinder.find(request.getSettlementType()).settle(request);
    return ResponseEntity.created(URI.create("/settle/" + settleId)).build();
  }
}
