package com.jindo.minipay.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class NotificationCommonDto {

  @NotNull
  private Long memberId;

  @NotNull
  private String title;

  @NotNull
  private String content;
}
