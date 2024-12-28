package com.jindo.minipay.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCommonDto {

  @NotNull
  private Long memberId;

  @NotNull
  private String title;

  @NotNull
  private String content;
}
