package com.jindo.minipay.integration;

import static com.jindo.minipay.account.common.type.AccountType.CHECKING;
import static com.jindo.minipay.account.common.type.AccountType.SAVING;
import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.util.AccountNumberCreator;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateRequest;
import com.jindo.minipay.account.savings.dto.SavingAccountDepositRequest;
import com.jindo.minipay.account.savings.entity.SavingAccount;
import com.jindo.minipay.account.savings.repository.SavingAccountRepository;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import io.restassured.RestAssured;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("적금 계좌 통합 테스트")
class SavingAccountIntegrationTest extends IntegrationTestSupport {

  private static final String URI = "/saving";
  private static final long DEFAULT_AMOUNT = 50000L;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  SavingAccountRepository savingAccountRepository;

  @Autowired
  CheckingAccountRepository checkingAccountRepository;

  @Autowired
  AccountNumberCreator accountNumberCreator;

  @Test
  @DisplayName("적금 계좌를 생성한다.")
  void createSavingAccount() {
    // given
    Member owner = saveAndGetMember("testUser1");

    int beforeSize = savingAccountRepository.findByOwnerId(owner.getId()).size();

    SavingAccountCreateRequest request = SavingAccountCreateRequest.builder()
        .memberId(owner.getId())
        .build();

    // when
    RestAssured
        .given().log().all()
        .contentType(APPLICATION_JSON_VALUE)
        .body(request)
        .when().post(URI)
        .then().log().all()
        .statusCode(SC_CREATED);

    // then
    List<SavingAccount> savingAccounts = savingAccountRepository.findByOwnerId(owner.getId());
    assertThat(savingAccounts).hasSize(beforeSize + 1);
  }

  @Test
  @DisplayName("적금 계좌에 납입한다.")
  void depositAmount() {
    // given
    Member owner = saveAndGetMember("testUser1");
    saveCheckingAccount(owner);
    SavingAccount savingAccount = saveAndGetSavingAccount(owner);

    SavingAccountDepositRequest request = SavingAccountDepositRequest.builder()
        .ownerId(owner.getId())
        .savingAccountId(savingAccount.getId())
        .amount(10000L)
        .build();

    // when
    RestAssured
        .given().log().all()
        .contentType(APPLICATION_JSON_VALUE)
        .body(request)
        .when().post(URI + "/deposit")
        .then().log().all()
        .body("amount", is(10000))
        .statusCode(SC_OK);

    // then
    Optional<CheckingAccount> checkingAccount = checkingAccountRepository.findByOwnerId(
        owner.getId());
    assertThat(checkingAccount.isPresent()).isTrue();
    assertThat(checkingAccount.get().getBalance()).isEqualTo(DEFAULT_AMOUNT - request.getAmount());

    Optional<SavingAccount> updatedSavingAccount = savingAccountRepository.findById(
        savingAccount.getId());
    assertThat(updatedSavingAccount.isPresent()).isTrue();
    assertThat(updatedSavingAccount.get().getAmount()).isEqualTo(request.getAmount());
  }

  private void saveCheckingAccount(Member owner) {
    checkingAccountRepository.save(
        CheckingAccount.builder()
            .owner(owner)
            .accountNumber(accountNumberCreator.create(CHECKING))
            .balance(DEFAULT_AMOUNT)
            .build());
  }

  private SavingAccount saveAndGetSavingAccount(Member owner) {
    return savingAccountRepository.save(
        SavingAccount.builder()
            .owner(owner)
            .accountNumber(accountNumberCreator.create(SAVING))
            .build());
  }
}
