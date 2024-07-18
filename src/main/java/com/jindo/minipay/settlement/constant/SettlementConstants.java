package com.jindo.minipay.settlement.constant;

public class SettlementConstants {

  public static final int SETTLEMENT_PARTICIPANTS_MIN_SIZE = 2;
  public static final int SETTLEMENT_PARTICIPANTS_MAX_SIZE = 50;

  public static final long SETTLEMENT_DISTRIBUTE_MIN_AMOUNT = 1L;
  public static final long SETTLEMENT_DISTRIBUTE_MAX_AMOUNT = 5_000_000L;

  public static final long RANDOM_SETTLEMENT_MIN_AMOUNT = 1_000L;
  public static final long RANDOM_SETTLEMENT_UNIT = 100L;
}
