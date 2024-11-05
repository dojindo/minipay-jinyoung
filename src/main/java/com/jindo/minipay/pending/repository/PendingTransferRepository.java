package com.jindo.minipay.pending.repository;

import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.pending.entity.PendingTransfer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingTransferRepository extends JpaRepository<PendingTransfer, Long> {

  Optional<PendingTransfer> findBySender(Member sender);
}
