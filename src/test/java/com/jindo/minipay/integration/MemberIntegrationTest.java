package com.jindo.minipay.integration;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.member.dto.MemberSignupRequest;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import io.restassured.RestAssured;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("회원 통합 테스트")
class MemberIntegrationTest extends IntegrationTestSupport {

  private static final String URI = "/member";

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  CheckingAccountRepository checkingAccountRepository;

  @Test
  @DisplayName("회원 가입 요청이 들어오면 회원을 등록하고, 해당 회원의 메인 계좌가 생성된다.")
  void signup() {
    String username = "testUser";

    // given
    MemberSignupRequest request = MemberSignupRequest.builder()
        .username(username)
        .password("1234")
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
    Optional<Member> savedMember = memberRepository.findByUsername(username);
    assertThat(savedMember).isPresent();
    assertThat(savedMember.get().getUsername()).isEqualTo(username);

    Optional<CheckingAccount> savedCheckingAccount = checkingAccountRepository.findByOwnerId(
        savedMember.get().getId());
    assertThat(savedCheckingAccount).isPresent();
    assertThat(savedCheckingAccount.get().getOwner().getId()).isEqualTo(savedMember.get().getId());
    assertThat(savedCheckingAccount.get().getBalance()).isZero();
  }
}
