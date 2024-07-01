package com.jindo.minipay.account.concurrent;

import static org.assertj.core.api.Assertions.assertThat;

import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.checking.service.CheckingAccountService;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateRequest;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateResponse;
import com.jindo.minipay.account.savings.dto.SavingAccountDepositRequest;
import com.jindo.minipay.account.savings.entity.SavingAccount;
import com.jindo.minipay.account.savings.repository.SavingAccountRepository;
import com.jindo.minipay.account.savings.service.SavingAccountService;
import com.jindo.minipay.member.dto.MemberSignupRequest;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.member.service.MemberService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AccountConcurrentTest {

  @Autowired
  MemberService memberService;

  @Autowired
  SavingAccountService savingAccountService;

  @Autowired
  CheckingAccountService checkingAccountService;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  SavingAccountRepository savingAccountRepository;

  @Autowired
  CheckingAccountRepository checkingAccountRepository;

  @Test
  @DisplayName("메인계좌 충전, 적금계좌 입금 동시성 테스트")
  void concurrent_charge_deposit() throws InterruptedException {
    // given

    // 회원등록
    String username = "concurrentTestUser";
    memberService.signup(new MemberSignupRequest(username, "1q2w3e4r!"));
    Member owner = memberRepository.findByUsername(username).get();

    // 메인 계좌 개설 및 충전
    CheckingAccount savedCheckingAccount = checkingAccountRepository.findByOwnerId(owner.getId())
        .get();
    checkingAccountService.charge(new CheckingAccountChargeRequest(owner.getId(), 100_000L));

    // 적금 계좌 개설
    SavingAccountCreateResponse savingAccountCreateResponse = savingAccountService.create(
        new SavingAccountCreateRequest(owner.getId()));

    // 메인 계좌 충전 request
    CheckingAccountChargeRequest chargeRequest =
        new CheckingAccountChargeRequest(owner.getId(), 10_000L);

    // 적금 계좌 충전 request
    SavingAccountDepositRequest depositRequest =
        new SavingAccountDepositRequest(owner.getId(), savingAccountCreateResponse.getSavingAccountId(),
            10_000L);

    int nThreads = 10;
    int repeat = 5;
    ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
    CountDownLatch countDownLatch = new CountDownLatch(repeat);

    // when
    for (int i = 0; i < repeat; i++) {
      executorService.execute(() -> {
        checkingAccountService.charge(chargeRequest);
        savingAccountService.deposit(depositRequest);
        countDownLatch.countDown();
      });
    }

    countDownLatch.await();
    executorService.shutdown();

    // then
    CheckingAccount checkingAccount = checkingAccountRepository.findById(
        savedCheckingAccount.getId()).get();
    SavingAccount savingAccount = savingAccountRepository.findById(
        savingAccountCreateResponse.getSavingAccountId()).get();
    assertThat(checkingAccount.getBalance()).isEqualTo(100_000L);
    assertThat(savingAccount.getAmount()).isEqualTo(50_000L);
  }
}
