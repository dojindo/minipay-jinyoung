package com.jindo.minipay.account.checking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.jindo.minipay.member.entity.MemberSettings;
import com.jindo.minipay.member.repository.MemberSettingsRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RemitServiceFinderTest {

  @InjectMocks
  RemitServiceFinder remitServiceFinder;

  @Spy
  List<RemitService> remitServices = new ArrayList<>();

  @Mock
  ImmediateRemitService immediateRemitService;

  @Mock
  PendingRemitService pendingRemitService;

  @Mock
  MemberSettingsRepository memberSettingsRepository;

  @BeforeEach
  void setUp() {
    remitServices.add(immediateRemitService);
    remitServices.add(pendingRemitService);
  }

  @Nested
  @DisplayName("송신자의 송금 설정에 따라 송금 서비스 구현체를 찾는다.")
  class FindMethod {

    @Test
    @DisplayName("송신자의 즉시 송금 설정이 ON 이면 '즉시 송금 서비스'를 찾는다.")
    void findImmediateTransferService() {
      // given
      Long senderId = 1L;

      MemberSettings settings = MemberSettings.builder()
          .immediateTransferEnabled(true)
          .build();

      given(memberSettingsRepository.findByMemberId(senderId)).willReturn(Optional.of(settings));
      given(immediateRemitService.isImmediateTransfer()).willReturn(true);

      // when
      RemitService result = remitServiceFinder.find(senderId);

      // then
      assertThat(result).isEqualTo(immediateRemitService);
    }

    @Test
    @DisplayName("송신자의 즉시 송금 설정이 OFF 면 '보류 송금 서비스'를 찾는다.")
    void findPendingTransferService() {
      // given
      Long senderId = 1L;

      MemberSettings settings = MemberSettings.builder()
          .immediateTransferEnabled(false)
          .build();

      given(memberSettingsRepository.findByMemberId(senderId)).willReturn(Optional.of(settings));
      given(immediateRemitService.isImmediateTransfer()).willReturn(true);
      given(pendingRemitService.isImmediateTransfer()).willReturn(false);

      // when
      RemitService result = remitServiceFinder.find(senderId);

      // then
      assertThat(result).isEqualTo(pendingRemitService);
    }
  }
}