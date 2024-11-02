package com.jindo.minipay.member.service;

import static com.jindo.minipay.global.exception.ErrorCode.ALREADY_EXISTS_USERNAME;

import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.dto.MemberSignupRequest;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.entity.MemberSettings;
import com.jindo.minipay.member.event.MemberSignupEvent;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.member.repository.MemberSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberSettingsRepository memberSettingsRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public Long signup(MemberSignupRequest memberSignupRequest) {
    if (memberRepository.existsByUsername(memberSignupRequest.getUsername())) {
      throw new CustomException(ALREADY_EXISTS_USERNAME);
    }

    Member member = memberRepository.save(memberSignupRequest.toEntity());

    saveMemberSettings(member);

    eventPublisher.publishEvent(MemberSignupEvent.of(member.getId()));

    return member.getId();
  }

  private void saveMemberSettings(Member member) {
    MemberSettings settings = MemberSettings.builder()
        .member(member)
        .immediateTransferEnabled(false)
        .build();

    memberSettingsRepository.save(settings);
  }
}
