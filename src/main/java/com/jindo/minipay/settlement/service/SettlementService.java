package com.jindo.minipay.settlement.service;

import static com.jindo.minipay.global.exception.ErrorCode.INVALID_PARTICIPANT;
import static com.jindo.minipay.global.exception.ErrorCode.MEMBER_NOT_FOUND;

import com.jindo.minipay.settlement.dto.SettleDistributeRequest;
import com.jindo.minipay.settlement.dto.SettleDistributeResponse;
import com.jindo.minipay.settlement.type.SettlementType;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.settlement.dto.SettleRequest;
import com.jindo.minipay.settlement.entity.ParticipantSettlement;
import com.jindo.minipay.settlement.entity.Settlement;
import com.jindo.minipay.settlement.repository.ParticipantSettlementRepository;
import com.jindo.minipay.settlement.repository.SettlementRepository;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public abstract class SettlementService {

  private final MemberRepository memberRepository;
  private final SettlementRepository settlementRepository;
  private final ParticipantSettlementRepository participantSettlementRepository;

  public abstract SettleDistributeResponse distribute(SettleDistributeRequest request);

  public abstract SettlementType getType();

  @Transactional
  public Long settle(SettleRequest request) {
    List<Member> participants = getParticipants(request.getParticipantIds());
    long[] amounts = request.getAmounts();

    Member requester = getMember(request.getRequesterId());

    Settlement savedSettlement = settlementRepository.save(request.toEntity(requester));

    saveParticipantSettlement(participants, amounts, savedSettlement);
    return savedSettlement.getId();
  }

  private List<Member> getParticipants(List<Long> participantsId) {
    List<Member> participants = memberRepository.findByIdIn(participantsId);

    if (participants.size() != participantsId.size()) {
      throw new CustomException(INVALID_PARTICIPANT);
    }

    return participants;
  }

  private Member getMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
  }

  private void saveParticipantSettlement(List<Member> participants, long[] amounts,
      Settlement savedSettlement) {
    IntStream.range(0, amounts.length).forEach(i -> {
      Member participant = participants.get(i);
      long amount = amounts[i];

      participantSettlementRepository.save(
          ParticipantSettlement.of(savedSettlement, participant, amount));
    });
  }
}
