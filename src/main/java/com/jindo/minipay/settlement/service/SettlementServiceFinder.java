package com.jindo.minipay.settlement.service;

import static com.jindo.minipay.global.exception.ErrorCode.NOT_EXISTS_SETTLE_TYPE;

import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.settlement.type.SettlementType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementServiceFinder {

  private final List<SettlementService> settlementServices;

  public SettlementService find(SettlementType settlementType) {
    return settlementServices.stream().filter(settlementService -> settlementService.getType() == settlementType)
        .findAny().orElseThrow(() -> new CustomException(NOT_EXISTS_SETTLE_TYPE));
  }
}
