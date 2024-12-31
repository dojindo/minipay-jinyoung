package com.jindo.minipay.notification.fcm.controller;

import com.jindo.minipay.notification.fcm.dto.FcmTokenRegisterRequest;
import com.jindo.minipay.notification.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmController {

  private final FcmService fcmService;

  /**
   * FCM 토큰 등록
   */
  @PostMapping("/register")
  public ResponseEntity<Void> registerToken(@RequestBody FcmTokenRegisterRequest request) {
    fcmService.registerToken(request);
    return ResponseEntity.ok().build();
  }
}
