package com.jindo.minipay.settlement.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

/**
 * EQUAL: 1/N으로 금액을 나눠서 정산
 * RANDOM: 랜덤으로 금액을 나눠서 정산
 */
public enum SettlementType {
  EQUAL, RANDOM;

  @JsonCreator
  public static SettlementType initialize(String value) {
    return Arrays.stream(SettlementType.values())
        .filter(enumValue -> enumValue.name().equals(value))
        .findFirst()
        .orElse(null);
  }
}
