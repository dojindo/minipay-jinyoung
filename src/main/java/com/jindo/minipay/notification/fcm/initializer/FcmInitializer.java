package com.jindo.minipay.notification.fcm.initializer;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FcmInitializer {

  @Value("${fcm.service-account-key-path}")
  private String serviceAccountKeyPath;

  @Value("${fcm.project-id}")
  private String projectId;

  @PostConstruct
  public void initialize() throws IOException {
      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.fromStream(
              new ClassPathResource(serviceAccountKeyPath).getInputStream()))
          .setProjectId(projectId)
          .build();
      FirebaseApp.initializeApp(options);
      log.info("FCM initialize() success");
  }
}
