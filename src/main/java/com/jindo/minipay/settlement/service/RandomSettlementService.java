package com.jindo.minipay.settlement.service;

import static com.jindo.minipay.settlement.constant.SettlementConstants.RANDOM_SETTLEMENT_UNIT;

import com.jindo.minipay.settlement.dto.SettleDistributeRequest;
import com.jindo.minipay.settlement.dto.SettleDistributeResponse;
import com.jindo.minipay.settlement.type.SettlementType;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.settlement.repository.ParticipantSettlementRepository;
import com.jindo.minipay.settlement.repository.SettlementRepository;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class RandomSettlementService extends SettlementService {

  public RandomSettlementService(
      MemberRepository memberRepository,
      SettlementRepository settlementRepository,
      ParticipantSettlementRepository participantSettlementRepository) {
    super(memberRepository, settlementRepository, participantSettlementRepository);
  }

  @Override
  public SettlementType getType() {
    return SettlementType.RANDOM;
  }

  @Override
  public SettleDistributeResponse distribute(SettleDistributeRequest request) {
    long[] settleAmounts = splitAmountAndShuffle(
        request.getTotalAmount(), request.getParticipantsSize());

    return SettleDistributeResponse.ofRandomSettle(settleAmounts);
  }

  private long[] splitAmountAndShuffle(long totalAmount, int participantsSize) {
    long[] settleAmounts = new long[participantsSize];
    Random random = new Random();

    int index = 0;
    while (totalAmount != 0 && index != participantsSize) {
      if (index == participantsSize - 1) {
        settleAmounts[index] = totalAmount;
        break;
      }

      long share = totalAmount / RANDOM_SETTLEMENT_UNIT;
      long amountPerParticipant = random.nextLong(share) * RANDOM_SETTLEMENT_UNIT;

      if (amountPerParticipant == 0) {
        amountPerParticipant = totalAmount % RANDOM_SETTLEMENT_UNIT;
      }

      settleAmounts[index++] = amountPerParticipant;
      totalAmount -= amountPerParticipant;
    }

    shuffleArray(settleAmounts);
    return settleAmounts;
  }

  private void shuffleArray(long[] arr) {
    Random random = new Random();
    for (int i = arr.length - 1; i > 0; i--) {
      int index = random.nextInt(i + 1);

      long temp = arr[index];
      arr[index] = arr[i];
      arr[i] = temp;
    }
  }
}
