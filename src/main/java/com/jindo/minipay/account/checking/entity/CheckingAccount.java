package com.jindo.minipay.account.checking.entity;

import static com.jindo.minipay.global.exception.ErrorCode.BALANCE_NOT_ENOUGH;

import com.jindo.minipay.global.entity.BaseTimeEntity;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckingAccount extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String accountNumber;

  private long balance;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private Member owner;

  public void deposit(long amount) {
    balance += amount;
  }

  public void withdraw(long amount) {
    if (balance < amount) {
      throw new CustomException(BALANCE_NOT_ENOUGH);
    }
    this.balance -= amount;
  }
}
