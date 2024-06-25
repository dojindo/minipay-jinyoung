package com.jindo.minipay.account.entity;

import com.jindo.minipay.account.entity.type.AccountType;
import com.jindo.minipay.global.entity.BaseTimeEntity;
import com.jindo.minipay.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Account extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String accountNumber;

  private long balance;

  @ManyToOne
  @JoinColumn(nullable = false)
  private Member owner;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountType type;
}
