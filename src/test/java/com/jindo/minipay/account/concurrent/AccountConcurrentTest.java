package com.jindo.minipay.account.concurrent;

import static org.assertj.core.api.Assertions.assertThat;

import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
import com.jindo.minipay.account.checking.dto.CheckingAccountRemitRequest;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.service.CheckingAccountService;
import com.jindo.minipay.account.savings.dto.SavingAccountDepositRequest;
import com.jindo.minipay.account.savings.entity.SavingAccount;
import com.jindo.minipay.account.savings.service.SavingAccountService;
import com.jindo.minipay.integration.IntegrationTestSupport;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.service.MemberService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AccountConcurrentTest extends IntegrationTestSupport {

  @Autowired
  MemberService memberService;

  @Autowired
  SavingAccountService savingAccountService;

  @Autowired
  CheckingAccountService checkingAccountService;

  static final String ME = "me";
  static final String FRIEND = "friend";

  @Test
  @DisplayName("메인계좌 충전, 적금계좌 입금 동시성 테스트")
  void charge_saving_deposit_concurrent() throws InterruptedException {
    // given
    Member me = saveAndGetMember(ME);

    saveCheckingAccount(me, 500_000L);

    SavingAccount mySavingAccount = saveSavingAccount(me);

    // 내 메인 계좌 충전 request
    CheckingAccountChargeRequest myChargeRequest =
        new CheckingAccountChargeRequest(me.getId(), 10_000L);

    // 내 적금 계좌 입금 request
    SavingAccountDepositRequest myDepositRequest =
        new SavingAccountDepositRequest(me.getId(), mySavingAccount.getId(), 10_000L);

    int nThreads = 30;
    int repeat = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
    CountDownLatch countDownLatch = new CountDownLatch(repeat);

    // when
    for (int i = 0; i < repeat; i++) {
      final int finalI = i;
      executorService.execute(() -> {
        if (finalI % 2 == 0) {
          checkingAccountService.charge(myChargeRequest);
        } else {
          savingAccountService.deposit(myDepositRequest);
        }
        countDownLatch.countDown();
      });
    }

    countDownLatch.await();
    executorService.shutdown();

    CheckingAccount myCheckingAccount =
        checkingAccountRepository.findByOwnerId(me.getId()).get();
    mySavingAccount = savingAccountRepository.findByOwnerId(me.getId()).get(0);

    // then
    // 초기 금액 500_000 + (10_000원 충전 * 5) - (10_000원 적금 * 5) = 500_000
    assertThat(myCheckingAccount.getBalance()).isEqualTo(500_000L);
    assertThat(mySavingAccount.getAmount()).isEqualTo(500_000L);
  }

  @Test
  @DisplayName("메인계좌 충전, 친구에게 송금 동시성 테스트")
  void charge_remit_concurrent() throws InterruptedException {
    // given
    Member me = saveAndGetMember(ME);
    Member friend = saveAndGetMember(FRIEND);

    saveCheckingAccount(me, 1_000_000L);
    saveCheckingAccount(friend, 1_000_000L);

    // 내 메인 계좌 충전 request
    CheckingAccountChargeRequest myChargeRequest =
        new CheckingAccountChargeRequest(me.getId(), 10_000L);

    // 친구 메인 계좌 충전 request
    CheckingAccountChargeRequest friendChargeRequest =
        new CheckingAccountChargeRequest(friend.getId(), 10_000L);

    // 둘 모두 즉시송금으로 변경
    enableImmediateTransferOfMember(me);
    enableImmediateTransferOfMember(friend);

    // 내가 친구에게 송금 request
    CheckingAccountRemitRequest myRemitRequest = new CheckingAccountRemitRequest(
        me.getId(), friend.getId(), 10_000L);

    // 친구가 나에게 송금
    CheckingAccountRemitRequest friendRemitRequest = new CheckingAccountRemitRequest(
        friend.getId(), me.getId(), 10_000L);

    int nThreads = 30;
    int repeat = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
    CountDownLatch countDownLatch = new CountDownLatch(repeat * 4);

    // when
    for (int i = 0; i < repeat; i++) {
      executorService.execute(() -> {
        checkingAccountService.charge(myChargeRequest); // 내 계좌 충전
        countDownLatch.countDown();
      });

      executorService.execute(() -> {
        checkingAccountService.charge(friendChargeRequest); // 친구 계좌 충전
        countDownLatch.countDown();
      });

      executorService.execute(() -> {
        checkingAccountService.remit(myRemitRequest); // 나 -> 친구 송금
        countDownLatch.countDown();
      });

      executorService.execute(() -> {
        checkingAccountService.remit(friendRemitRequest); // 친구 -> 나 송금
        countDownLatch.countDown();
      });
    }

    countDownLatch.await();
    executorService.shutdown();

    // then
    CheckingAccount myCheckingAccount =
        checkingAccountRepository.findByOwnerId(me.getId()).get();

    CheckingAccount friendCheckingAccount =
        checkingAccountRepository.findByOwnerId(friend.getId()).get();

    assertThat(myCheckingAccount.getBalance()).isEqualTo(2_000_000L);
    assertThat(friendCheckingAccount.getBalance()).isEqualTo(2_000_000L);
  }

  @Test
  @DisplayName("메인계좌 충전, 친구에게 송금 데드락 테스트")
  void charge_remit_concurrent_deadlock() throws InterruptedException {
    // given
    Member me = saveAndGetMember(ME);
    Member friend = saveAndGetMember(FRIEND);

    saveCheckingAccount(me, 0L);
    saveCheckingAccount(friend, 0L);

    // 내 메인 계좌 충전 request
    CheckingAccountChargeRequest myChargeRequest =
        new CheckingAccountChargeRequest(me.getId(), 10_000L);

    // 친구 메인 계좌 충전 request
    CheckingAccountChargeRequest friendChargeRequest =
        new CheckingAccountChargeRequest(friend.getId(), 10_000L);

    // 둘 모두 즉시송금으로 변경
    enableImmediateTransferOfMember(me);
    enableImmediateTransferOfMember(friend);

    // 내가 친구에게 송금 request
    CheckingAccountRemitRequest myRemitRequest = new CheckingAccountRemitRequest(
        me.getId(), friend.getId(), 20_000L);

    // 친구가 나에게 송금
    CheckingAccountRemitRequest friendRemitRequest = new CheckingAccountRemitRequest(
        friend.getId(), me.getId(), 20_000L);

    int nThreads = 30;
    int repeat = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
    CountDownLatch countDownLatch = new CountDownLatch(repeat * 4);

    // when
    for (int i = 0; i < repeat; i++) {
      executorService.execute(() -> {
        checkingAccountService.charge(myChargeRequest); // 내 계좌 충전
        countDownLatch.countDown();
      });

      executorService.execute(() -> {
        checkingAccountService.charge(friendChargeRequest); // 친구 계좌 충전
        countDownLatch.countDown();
      });

      executorService.execute(() -> {
        checkingAccountService.remit(myRemitRequest); // 나 -> 친구 송금
        countDownLatch.countDown();
      });

      executorService.execute(() -> {
        checkingAccountService.remit(friendRemitRequest); // 친구 -> 나 송금
        countDownLatch.countDown();
      });
    }

    countDownLatch.await();
    executorService.shutdown();

    // then
    // 4개의 요청을 100번동안 반복했을 때, 데드락이 발생하지 않으면 테스트가 통과한 것으로 간주.
    // 각자 100만원 충전, 서로에게 200만원 송금
    // 두 계좌를 합쳐서 봤을 때, 200만원 이상이어야한다.
    CheckingAccount myCheckingAccount =
        checkingAccountRepository.findByOwnerId(me.getId()).get();
    CheckingAccount friendCheckingAccount =
        checkingAccountRepository.findByOwnerId(friend.getId()).get();
    long sumOfBalance = myCheckingAccount.getBalance() + friendCheckingAccount.getBalance();
    Assertions.assertThat(sumOfBalance).isGreaterThanOrEqualTo(2_000_000L);
  }

  @Test
  @DisplayName("메인계좌 충전, 적금계좌 입금, 송금 동시성 테스트")
  void charge_saving_deposit_remit_concurrent() throws InterruptedException {
    // given
    Member me = saveAndGetMember(ME);
    Member friend = saveAndGetMember(FRIEND);

    saveCheckingAccount(me, 1_000_000L);
    saveCheckingAccount(friend, 1_000_000L);

    SavingAccount mySavingAccount = saveSavingAccount(me);
    SavingAccount friendSavingAccount = saveSavingAccount(friend);

    // 내 메인 계좌 충전 request
    CheckingAccountChargeRequest myChargeRequest =
        new CheckingAccountChargeRequest(me.getId(), 10_000L);

    // 친구 메인 계좌 충전 request
    CheckingAccountChargeRequest friendChargeRequest =
        new CheckingAccountChargeRequest(friend.getId(), 10_000L);

    // 내 적금 계좌 입금 request
    SavingAccountDepositRequest myDepositRequest =
        new SavingAccountDepositRequest(me.getId(), mySavingAccount.getId(), 10_000L);

    // 내 적금 계좌 입금 request
    SavingAccountDepositRequest friendDepositRequest =
        new SavingAccountDepositRequest(friend.getId(), friendSavingAccount.getId(), 10_000L);

    // 둘 모두 즉시송금으로 변경
    enableImmediateTransferOfMember(me);
    enableImmediateTransferOfMember(friend);

    // 내가 친구에게 송금 request
    CheckingAccountRemitRequest myRemitRequest = new CheckingAccountRemitRequest(
        me.getId(), friend.getId(), 10_000L);

    // 친구가 나에게 송금
    CheckingAccountRemitRequest friendRemitRequest = new CheckingAccountRemitRequest(
        friend.getId(), me.getId(), 10_000L);

    int nThreads = 30;
    int repeat = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
    CountDownLatch countDownLatch = new CountDownLatch(repeat * 6);

    // when
    for (int i = 0; i < repeat; i++) {
      executorService.execute(() -> {
        checkingAccountService.charge(myChargeRequest); // 내 계좌 충전
        countDownLatch.countDown();
      });

      executorService.execute(() -> {
        checkingAccountService.charge(friendChargeRequest); // 친구 계좌 충전
        countDownLatch.countDown();
      });

      executorService.execute(() -> {
        savingAccountService.deposit(myDepositRequest); // 나의 적금
        countDownLatch.countDown();
      });

      executorService.execute(() -> {
        savingAccountService.deposit(friendDepositRequest); // 친구의 적금
        countDownLatch.countDown();
      });

      executorService.execute(() -> {
        checkingAccountService.remit(myRemitRequest); // 나 -> 친구 송금
        countDownLatch.countDown();
      });

      executorService.execute(() -> {
        checkingAccountService.remit(friendRemitRequest); // 친구 -> 나 송금
        countDownLatch.countDown();
      });
    }

    countDownLatch.await();
    executorService.shutdown();

    CheckingAccount myCheckingAccount = checkingAccountRepository.findByOwnerId(me.getId()).get();
    CheckingAccount friendCheckingAccount = checkingAccountRepository.findByOwnerId(friend.getId())
        .get();

    mySavingAccount = savingAccountRepository.findByOwnerId(me.getId()).get(0);
    friendSavingAccount = savingAccountRepository.findByOwnerId(friend.getId()).get(0);

    // then
    // 초기 금액 := 1_000_000 -> + (10000원 충전 * 100) - (10000원 적금 * 100) - (서로에게 10000원 송금 * 100)
    assertThat(myCheckingAccount.getBalance()).isEqualTo(1_000_000L);
    assertThat(friendCheckingAccount.getBalance()).isEqualTo(1_000_000L);

    assertThat(mySavingAccount.getAmount()).isEqualTo(1_000_000L);
    assertThat(friendSavingAccount.getAmount()).isEqualTo(1_000_000L);
  }
}
