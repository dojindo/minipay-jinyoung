package com.jindo.minipay.notification;

import com.jindo.minipay.notification.dto.NotificationCommonDto;
import com.jindo.minipay.notification.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class FcmTestController {

  private final FcmService fcmService;

  @GetMapping("/test")
  public String test() {
    return "index.html";
  }

  /**
   * FCM 메시지 전송 테스트
   */
  @PostMapping("/test")
  @ResponseBody
  public ResponseEntity<Void> sendMessage(@RequestBody NotificationCommonDto dto) {
    // TODO: Service 클래스로 옮기기, FCM 전용 DTO 클래스 작성, fromCommonDto(dto) 메서드 포함
    fcmService.sendNotification(dto);
    return ResponseEntity.ok().build();
  }
}
