package com.jindo.minipay.member.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberSignupEvent {

  private Long memberId;

  public static MemberSignupEvent of(Long memberId) {
    return new MemberSignupEvent(memberId);
  }
}
