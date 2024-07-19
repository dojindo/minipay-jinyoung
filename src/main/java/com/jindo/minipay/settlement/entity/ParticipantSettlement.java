package com.jindo.minipay.settlement.entity;

import com.jindo.minipay.global.entity.BaseTimeEntity;
import com.jindo.minipay.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantSettlement extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(nullable = false)
  private Settlement settlement;

  @ManyToOne
  @JoinColumn(nullable = false)
  private Member participant;

  @Column(nullable = false)
  private long amount;

  public static ParticipantSettlement of(Settlement savedSettlement, Member participant, long amount) {
    return ParticipantSettlement.builder()
        .settlement(savedSettlement)
        .participant(participant)
        .amount(amount)
        .build();
  }
}
