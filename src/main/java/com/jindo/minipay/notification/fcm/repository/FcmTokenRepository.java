package com.jindo.minipay.notification.fcm.repository;

import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.notification.fcm.entity.FcmToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
  Optional<FcmToken> findByMember(Member member);
}
