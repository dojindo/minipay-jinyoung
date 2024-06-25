package com.jindo.minipay.member.repository;

import com.jindo.minipay.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByUsername(String username);

  boolean existsByUsername(String username);
}
