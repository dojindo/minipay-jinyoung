package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.global.exception.ErrorCode.INTERNAL_SERVER_ERROR;

import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.MemberSettings;
import com.jindo.minipay.member.repository.MemberSettingsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemitServiceFinder {

  private final List<RemitService> remitServices;
  private final MemberSettingsRepository memberSettingsRepository;

  public RemitService find(Long senderId) {
    MemberSettings senderSettings = memberSettingsRepository.findByMemberId(senderId)
        .orElseThrow(() -> new CustomException(INTERNAL_SERVER_ERROR));

    boolean immediateTransferEnabled = senderSettings.isImmediateTransferEnabled();
    return remitServices.stream()
        .filter(service -> service.isImmediateTransfer() == immediateTransferEnabled)
        .findAny()
        .orElseThrow(() -> new CustomException(INTERNAL_SERVER_ERROR));
  }
}
