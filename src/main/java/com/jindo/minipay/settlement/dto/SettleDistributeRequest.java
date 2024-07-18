package com.jindo.minipay.settlement.dto;

import static com.jindo.minipay.settlement.constant.SettlementConstants.*;

import com.jindo.minipay.settlement.type.SettlementType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SettleDistributeRequest {

  @NotNull
  private Long requesterId;

  @Min(SETTLEMENT_PARTICIPANTS_MIN_SIZE) @Max(SETTLEMENT_PARTICIPANTS_MAX_SIZE)
  private int participantsSize;

  @Min(SETTLEMENT_DISTRIBUTE_MIN_AMOUNT) @Max(SETTLEMENT_DISTRIBUTE_MAX_AMOUNT)
  private long totalAmount;

  @NotNull
  private SettlementType settlementType;

}
