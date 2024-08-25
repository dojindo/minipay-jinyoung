package com.jindo.minipay.integration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.savings.repository.SavingAccountRepository;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.settlement.repository.ParticipantSettlementRepository;
import com.jindo.minipay.settlement.repository.SettlementRepository;
import io.restassured.RestAssured;
import java.util.Set;
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
  protected CheckingAccountRepository checkingAccountRepository;

  @Autowired
  protected SavingAccountRepository savingAccountRepository;

  @Autowired
  protected SettlementRepository settlementRepository;

  @Autowired
  protected ParticipantSettlementRepository participantSettlementRepository;

  @Autowired
  protected RedisTemplate<String, Object> redisTemplate;

  @LocalServerPort
  int port;

  @BeforeEach
  void setup() {
    RestAssured.port = port;
  }

  @AfterEach
  void tearDown() {
    participantSettlementRepository.deleteAllInBatch();
    settlementRepository.deleteAllInBatch();
    checkingAccountRepository.deleteAllInBatch();
    savingAccountRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    Set<String> keys = redisTemplate.keys("*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  protected Member saveAndGetMember(String username) {
    return memberRepository.save(Member.builder()
        .username(username)
        .password("1q2w3e4r!")
        .build());
  }
}
