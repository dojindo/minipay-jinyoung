package com.jindo.minipay.member.repository;

import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.entity.MemberSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSettingsRepository extends JpaRepository<MemberSettings, Long> {

  Optional<MemberSettings> findByMember(Member member);
  Optional<MemberSettings> findByMemberId(Long memberId);
}
