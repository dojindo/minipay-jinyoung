package com.jindo.minipay.settlement.dto;

import java.util.Arrays;
import lombok.Getter;

@Getter
public class SettleDistributeResponse {

  private final long[] splitAmounts;

  private long supportsAmount;

  public static SettleDistributeResponse ofEqualSettle(int participantsSize, long splitAmount,
      long supportsAmount) {
    return new SettleDistributeResponse(participantsSize, splitAmount, supportsAmount);
  }

  public static SettleDistributeResponse ofRandomSettle(long[] splitAmounts) {
    return new SettleDistributeResponse(splitAmounts);
  }

  private SettleDistributeResponse(int participantsSize, long splitAmount, long supportsAmount) {
    this.splitAmounts = new long[participantsSize];
    Arrays.fill(splitAmounts, splitAmount);
    this.supportsAmount = supportsAmount;
  }

  private SettleDistributeResponse(long[] splitAmounts) {
    this.splitAmounts = splitAmounts;
  }
}
