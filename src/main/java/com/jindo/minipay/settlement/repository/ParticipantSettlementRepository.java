package com.jindo.minipay.settlement.repository;

import com.jindo.minipay.settlement.entity.ParticipantSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantSettlementRepository extends JpaRepository<ParticipantSettlement, Long> {

}
