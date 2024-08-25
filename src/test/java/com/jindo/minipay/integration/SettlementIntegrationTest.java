package com.jindo.minipay.integration;

import static com.jindo.minipay.settlement.type.SettlementType.EQUAL;
import static com.jindo.minipay.settlement.type.SettlementType.RANDOM;
import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.settlement.dto.SettleDistributeRequest;
import com.jindo.minipay.settlement.dto.SettleRequest;
import com.jindo.minipay.settlement.repository.ParticipantSettlementRepository;
import com.jindo.minipay.settlement.repository.SettlementRepository;
import io.restassured.RestAssured;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("정산 통합 테스트")
class SettlementIntegrationTest extends IntegrationTestSupport {

  private static final String URI = "/settle";

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  SettlementRepository settlementRepository;

  @Autowired
  ParticipantSettlementRepository participantSettlementRepository;

  @Test
  @DisplayName("회원마다 정산해야할 금액을 동일하게 분배한다.")
  void distributeEqualsSettlement() {
    // given
    Member requester = saveAndGetMember("requester");

    SettleDistributeRequest request = SettleDistributeRequest.builder()
        .requesterId(requester.getId())
        .participantsSize(3)
        .totalAmount(30001)
        .settlementType(EQUAL)
        .build();

    // when
    RestAssured
        .given().log().all()
        .contentType(APPLICATION_JSON_VALUE)
        .body(request)
        .when().post(URI + "/distribute")
        .then().log().all()
        .body("splitAmounts[0]", is(10000))
        .body("splitAmounts[1]", is(10000))
        .body("splitAmounts[2]", is(10000))
        .body("supportsAmount", is(1))
        .statusCode(SC_OK);
  }

  @Test
  @DisplayName("회원마다 정산해야할 금액을 랜덤하게 분배한다.")
  void distributeRandomsSettlement() {
    // given
    Member requester = saveAndGetMember("requester");

    SettleDistributeRequest request = SettleDistributeRequest.builder()
        .requesterId(requester.getId())
        .participantsSize(3)
        .totalAmount(30001)
        .settlementType(RANDOM)
        .build();

    // when
    RestAssured
        .given().log().all()
        .contentType(APPLICATION_JSON_VALUE)
        .body(request)
        .when().post(URI + "/distribute")
        .then().log().all()
        .body("splitAmounts", hasSize(3))
        .statusCode(SC_OK);
  }

  @Test
  @DisplayName("정산을 요청한다.")
  void settle() {
    // given
    Member requester = saveAndGetMember("requester");
    Member participant1 = saveAndGetMember("participant1");
    Member participant2 = saveAndGetMember("participant2");
    List<Long> participantsIds = new ArrayList<>(
        Arrays.asList(requester.getId(), participant1.getId(), participant2.getId()));

    long[] amounts = new long[]{3333, 3333, 3333};

    SettleRequest request = SettleRequest.builder()
        .requesterId(requester.getId())
        .participantIds(participantsIds)
        .totalAmount(10000)
        .supportsAmount(1)
        .amounts(amounts)
        .settlementType(EQUAL)
        .build();

    // when
    RestAssured
        .given().log().all()
        .contentType(APPLICATION_JSON_VALUE)
        .body(request)
        .when().post(URI)
        .then().log().all()
        .statusCode(SC_CREATED);
  }
}
