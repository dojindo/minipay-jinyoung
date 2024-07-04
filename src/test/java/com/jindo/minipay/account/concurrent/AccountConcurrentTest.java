package com.jindo.minipay.account.concurrent;

import static org.assertj.core.api.Assertions.assertThat;

import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
import com.jindo.minipay.account.checking.dto.CheckingAccountWireRequest;
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
  @DisplayName("메인계좌 충전, 적금계좌 입금, 친구에게 송금 동시성 테스트")
  void checking_account_concurrent() throws InterruptedException {
    // given

    // 회원등록
    String loginUsername = "loginUsername";
    memberService.signup(new MemberSignupRequest(loginUsername, "1q2w3e4r!"));
    Member loginUser = memberRepository.findByUsername(loginUsername).get();

    // 친구등록
    String friendUsername = "friendUsername";
    memberService.signup(new MemberSignupRequest(friendUsername, "1q2w3e4r!"));
    Member friend = memberRepository.findByUsername(friendUsername).get();

    // 메인 계좌 충전
    checkingAccountService.charge(new CheckingAccountChargeRequest(loginUser.getId(), 1_000_000L));

    checkingAccountService.charge(new CheckingAccountChargeRequest(friend.getId(), 1_000_000L));

    // 적금 계좌 개설
    SavingAccountCreateResponse loginUserSavingAccountCreateResponse = savingAccountService.create(
        new SavingAccountCreateRequest(loginUser.getId()));

    SavingAccountCreateResponse friendSavingAccountCreateResponse = savingAccountService.create(
        new SavingAccountCreateRequest(friend.getId()));

    // 로그인 유저 메인 계좌 충전 request
    CheckingAccountChargeRequest loginUserChargeRequest =
        new CheckingAccountChargeRequest(loginUser.getId(), 10_000L);

    // 친구 메인 계좌 충전 request
    CheckingAccountChargeRequest friendChargeRequest =
        new CheckingAccountChargeRequest(friend.getId(), 10_000L);

    // 로그인 유저 적금 계좌 입금 request
    SavingAccountDepositRequest loginUserDepositRequest =
        new SavingAccountDepositRequest(loginUser.getId(),
            loginUserSavingAccountCreateResponse.getSavingAccountId(),
            10_000L);

    // 친구 적금 계좌 입금 request
    SavingAccountDepositRequest friendDepositRequest =
        new SavingAccountDepositRequest(friend.getId(),
            friendSavingAccountCreateResponse.getSavingAccountId(),
            10_000L);

    // 로그인 유저 송금 request
    CheckingAccountWireRequest loginUserWireRequest = new CheckingAccountWireRequest(
        loginUser.getId(), friend.getId(), 10_000L);

    // 친구 송금 request
    CheckingAccountWireRequest friendUserWireRequest = new CheckingAccountWireRequest(
        friend.getId(), loginUser.getId(), 10_000L);

    int nThreads = 100;
    int repeat = 5;
    ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
    CountDownLatch countDownLatch = new CountDownLatch(repeat);

    // when
    for (int i = 0; i < repeat; i++) {
      executorService.execute(() -> {
        checkingAccountService.charge(loginUserChargeRequest);
        checkingAccountService.charge(friendChargeRequest);
        savingAccountService.deposit(loginUserDepositRequest);
        savingAccountService.deposit(friendDepositRequest);
        checkingAccountService.wire(loginUserWireRequest);
        checkingAccountService.wire(friendUserWireRequest);
        countDownLatch.countDown();
      });
    }

    countDownLatch.await();
    executorService.shutdown();

    // then
    CheckingAccount loginUserCheckingAccount = checkingAccountRepository.findByOwnerId(
        loginUser.getId()).get();

    CheckingAccount friendCheckingAccount = checkingAccountRepository.findByOwnerId(friend.getId())
        .get();

    SavingAccount loginUserSavingAccount = savingAccountRepository.findById(
        loginUserSavingAccountCreateResponse.getSavingAccountId()).get();

    SavingAccount friendSavingAccount = savingAccountRepository.findById(
        friendSavingAccountCreateResponse.getSavingAccountId()).get();

    // 메인계좌 100만원 (-)5 * 적금계좌입금, (+)5 * 메인계좌충전, (-)친구계좌에 송금, (+)친구계좌로부터 수신
    assertThat(loginUserCheckingAccount.getBalance()).isEqualTo(1_000_000L);
    assertThat(loginUserSavingAccount.getAmount()).isEqualTo(50_000L);

    assertThat(friendCheckingAccount.getBalance()).isEqualTo(1_000_000L);
    assertThat(friendSavingAccount.getAmount()).isEqualTo(50_000L);
  }
}
