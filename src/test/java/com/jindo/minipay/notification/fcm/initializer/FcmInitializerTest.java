package com.jindo.minipay.notification.fcm.initializer;

import com.google.firebase.FirebaseApp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FcmInitializerTest {
  
  @Test
  @DisplayName("실행 시 FirebaseApp을 초기화한다.")
  void testFirebaseAppInitialized() {
    Assertions.assertThat(FirebaseApp.getApps().isEmpty()).isFalse();
  }
}