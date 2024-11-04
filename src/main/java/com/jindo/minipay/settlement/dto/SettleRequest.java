package com.jindo.minipay.settlement.dto;

import static com.jindo.minipay.global.exception.ErrorCode.INSUFFICIENT_SETTLE_AMOUNT;
import static com.jindo.minipay.global.exception.ErrorCode.INVALID_SETTLE_REQUEST;
import static com.jindo.minipay.settlement.constant.SettlementConstants.RANDOM_SETTLEMENT_MIN_AMOUNT;
import static com.jindo.minipay.settlement.constant.SettlementConstants.SETTLEMENT_PARTICIPANTS_MAX_SIZE;
import static com.jindo.minipay.settlement.constant.SettlementConstants.SETTLEMENT_PARTICIPANTS_MIN_SIZE;
import static com.jindo.minipay.settlement.type.SettlementType.RANDOM;

import com.jindo.minipay.global.annotation.ValidEnum;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.settlement.entity.Settlement;
import com.jindo.minipay.settlement.type.SettlementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettleRequest {

  @NotNull
  private Long requesterId;

  @NotEmpty
  @Size(min = SETTLEMENT_PARTICIPANTS_MIN_SIZE, max = SETTLEMENT_PARTICIPANTS_MAX_SIZE)
  private List<Long> participantIds;

  @Min(SETTLEMENT_PARTICIPANTS_MIN_SIZE)
  private long totalAmount;

  @PositiveOrZero
  private long supportsAmount;

  @NotEmpty
  @Size(min = SETTLEMENT_PARTICIPANTS_MIN_SIZE, max = SETTLEMENT_PARTICIPANTS_MAX_SIZE)
  private long[] amounts;

  @ValidEnum(enumClass = SettlementType.class)
  private SettlementType settlementType;

  public Settlement toEntity(Member requester) {
    return Settlement.builder()
        .requester(requester)
        .totalAmount(totalAmount)
        .supportsAmount(supportsAmount)
        .settlementType(settlementType)
        .build();
  }

  public void additionalValidate() {
    if (getParticipantIds().size() != amounts.length) {
      throw new CustomException(INVALID_SETTLE_REQUEST);
    }

    if (getParticipantIds().size() > getTotalAmount()) {
      throw new CustomException(INSUFFICIENT_SETTLE_AMOUNT);
    }

    if (getSettlementType() == RANDOM && getTotalAmount() < RANDOM_SETTLEMENT_MIN_AMOUNT) {
      throw new CustomException(INSUFFICIENT_SETTLE_AMOUNT);
    }
  }
}
