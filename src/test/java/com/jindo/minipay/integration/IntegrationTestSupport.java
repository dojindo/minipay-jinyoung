package com.jindo.minipay.integration;

import static com.jindo.minipay.account.common.type.AccountType.CHECKING;
import static com.jindo.minipay.account.common.type.AccountType.SAVING;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.util.AccountNumberCreator;
import com.jindo.minipay.account.savings.entity.SavingAccount;
import com.jindo.minipay.account.savings.repository.SavingAccountRepository;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.entity.MemberSettings;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.member.repository.MemberSettingsRepository;
import com.jindo.minipay.pending.repository.PendingTransferRepository;
import com.jindo.minipay.settlement.repository.ParticipantSettlementRepository;
import com.jindo.minipay.settlement.repository.SettlementRepository;
import io.restassured.RestAssured;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class IntegrationTestSupport {

  @Autowired
  protected MemberRepository memberRepository;

  @Autowired
  protected MemberSettingsRepository memberSettingsRepository;

  @Autowired
  protected CheckingAccountRepository checkingAccountRepository;

  @Autowired
  protected SavingAccountRepository savingAccountRepository;

  @Autowired
  protected SettlementRepository settlementRepository;

  @Autowired
  protected ParticipantSettlementRepository participantSettlementRepository;

  @Autowired
  protected PendingTransferRepository pendingTransferRepository;

  @Autowired
  protected RedisTemplate<String, Object> redisTemplate;

  @Autowired
  protected AccountNumberCreator accountNumberCreator;

  @LocalServerPort
  int port;

  @BeforeEach
  void setup() {
    RestAssured.port = port;
  }

  @AfterEach
  void tearDown() {
    memberSettingsRepository.deleteAllInBatch();
    participantSettlementRepository.deleteAllInBatch();
    settlementRepository.deleteAllInBatch();
    checkingAccountRepository.deleteAllInBatch();
    savingAccountRepository.deleteAllInBatch();
    pendingTransferRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    Set<String> keys = redisTemplate.keys("*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  protected Member saveAndGetMember(String username) {
    Member member = memberRepository.save(Member.builder()
        .username(username)
        .password("1q2w3e4r!")
        .build());

    MemberSettings settings = MemberSettings.builder()
        .member(member)
        .immediateTransferEnabled(false)
        .build();

    memberSettingsRepository.save(settings);
    return member;
  }

  protected CheckingAccount saveCheckingAccount(Member owner, long balance) {
    return checkingAccountRepository.save(
        CheckingAccount.builder()
            .owner(owner)
            .accountNumber(accountNumberCreator.create(CHECKING))
            .balance(balance)
            .build());
  }

  protected SavingAccount saveSavingAccount(Member owner) {
    return savingAccountRepository.save(
        SavingAccount.builder()
            .owner(owner)
            .accountNumber(accountNumberCreator.create(SAVING))
            .amount(0L)
            .build());
  }

  protected void enableImmediateTransferOfMember(Member sender) {
    Optional<MemberSettings> senderSettings = memberSettingsRepository.findByMember(sender);
    Assertions.assertThat(senderSettings).isPresent();
    senderSettings.get().enableImmediateTransfer();
    memberSettingsRepository.save(senderSettings.get());
  }

  protected void disableImmediateTransferOfMember(Member sender) {
    Optional<MemberSettings> senderSettings = memberSettingsRepository.findByMember(sender);
    Assertions.assertThat(senderSettings).isPresent();
    senderSettings.get().disableImmediateTransfer();
    memberSettingsRepository.save(senderSettings.get());
  }
}
