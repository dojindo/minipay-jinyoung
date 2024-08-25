package com.jindo.minipay.settlement.dto;

import static com.jindo.minipay.settlement.constant.SettlementConstants.SETTLEMENT_DISTRIBUTE_MAX_AMOUNT;
import static com.jindo.minipay.settlement.constant.SettlementConstants.SETTLEMENT_DISTRIBUTE_MIN_AMOUNT;
import static com.jindo.minipay.settlement.constant.SettlementConstants.SETTLEMENT_PARTICIPANTS_MAX_SIZE;
import static com.jindo.minipay.settlement.constant.SettlementConstants.SETTLEMENT_PARTICIPANTS_MIN_SIZE;

import com.jindo.minipay.global.annotation.ValidEnum;
import com.jindo.minipay.settlement.type.SettlementType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SettleDistributeRequest {

  @NotNull
  private Long requesterId;

  @Min(SETTLEMENT_PARTICIPANTS_MIN_SIZE)
  @Max(SETTLEMENT_PARTICIPANTS_MAX_SIZE)
  private int participantsSize;

  @Min(SETTLEMENT_DISTRIBUTE_MIN_AMOUNT)
  @Max(SETTLEMENT_DISTRIBUTE_MAX_AMOUNT)
  private long totalAmount;

  @ValidEnum(enumClass = SettlementType.class)
  private SettlementType settlementType;
}
