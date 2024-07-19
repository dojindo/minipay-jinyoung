package com.jindo.minipay.settlement.service;

import com.jindo.minipay.settlement.dto.SettleDistributeRequest;
import com.jindo.minipay.settlement.dto.SettleDistributeResponse;
import com.jindo.minipay.settlement.type.SettlementType;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.settlement.repository.ParticipantSettlementRepository;
import com.jindo.minipay.settlement.repository.SettlementRepository;
import org.springframework.stereotype.Service;

@Service
public class EqualSettlementService extends SettlementService {

  public EqualSettlementService(MemberRepository memberRepository,
      SettlementRepository settlementRepository,
      ParticipantSettlementRepository participantSettlementRepository) {
    super(memberRepository, settlementRepository, participantSettlementRepository);
  }

  @Override
  public SettlementType getType() {
    return SettlementType.EQUAL;
  }

  @Override
  public SettleDistributeResponse distribute(SettleDistributeRequest request) {
    long splitAmount = request.getTotalAmount() / request.getParticipantsSize();
    long supportsAmount = request.getTotalAmount() % request.getParticipantsSize();

    return SettleDistributeResponse.ofEqualSettle(
        request.getParticipantsSize(), splitAmount, supportsAmount);
  }
}
