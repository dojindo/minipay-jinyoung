package com.jindo.minipay.integration;

import static com.jindo.minipay.account.common.type.AccountType.CHECKING;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
import com.jindo.minipay.account.checking.dto.CheckingAccountWireRequest;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.common.util.AccountNumberCreator;
import com.jindo.minipay.member.entity.Member;
import io.restassured.RestAssured;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("메인 계좌 통합 테스트")
public class CheckingAccountIntegrationTest extends IntegrationTestSupport {

  private static final String URI = "/account";
  private static final long DEFAULT_AMOUNT = 50000L;

  @Autowired
  AccountNumberCreator accountNumberCreator;

  @Test
  @DisplayName("회원이 충전을 요청하면 해당 회원의 메인 계좌에 충전 금액이 더해진다.")
  void chargeAmount() {
    // given
    Member owner = saveAndGetMember("testUser1");
    saveCheckingAccount(owner);

    CheckingAccountChargeRequest request = CheckingAccountChargeRequest.builder()
        .memberId(owner.getId())
        .amount(10000L)
        .build();

    // when
    RestAssured
        .given().log().all()
        .contentType(APPLICATION_JSON_VALUE)
        .body(request)
        .when().post(URI + "/charge")
        .then().log().all()
        .statusCode(SC_OK);

    // then
    Optional<CheckingAccount> checkingAccount = checkingAccountRepository.findByOwnerId(
        owner.getId());
    assertThat(checkingAccount.isPresent()).isTrue();
    assertThat(checkingAccount.get().getBalance())
        .isEqualTo(DEFAULT_AMOUNT + request.getAmount());
  }

  @Test
  @DisplayName("송금을 하면 송신자의 계좌에서 수신자의 계좌로 송금 금액만큼 빠져나간다.")
  void wireAmount() {
    // given
    Member sender = saveAndGetMember("testUser1");
    Member receiver = saveAndGetMember("testUser2");
    saveCheckingAccount(sender);
    saveCheckingAccount(receiver);

    CheckingAccountWireRequest request = CheckingAccountWireRequest.builder()
        .senderId(sender.getId())
        .receiverId(receiver.getId())
        .amount(10000L)
        .build();

    // when
    RestAssured
        .given().log().all()
        .contentType(APPLICATION_JSON_VALUE)
        .body(request)
        .when().post(URI + "/wire")
        .then().log().all()
        .statusCode(SC_OK);

    // then
    Optional<CheckingAccount> senderCheckingAccount =
        checkingAccountRepository.findByOwnerId(sender.getId());
    Optional<CheckingAccount> receiverCheckingAccount =
        checkingAccountRepository.findByOwnerId(receiver.getId());

    assertThat(senderCheckingAccount.isPresent()).isTrue();
    assertThat(receiverCheckingAccount.isPresent()).isTrue();

    assertThat(senderCheckingAccount.get().getBalance())
        .isEqualTo(DEFAULT_AMOUNT - request.getAmount());
    assertThat(receiverCheckingAccount.get().getBalance())
        .isEqualTo(DEFAULT_AMOUNT + request.getAmount());
  }

  private void saveCheckingAccount(Member owner) {
    checkingAccountRepository.save(
        CheckingAccount.builder()
            .owner(owner)
            .accountNumber(accountNumberCreator.create(CHECKING))
            .balance(DEFAULT_AMOUNT)
            .build());
  }
}