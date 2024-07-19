package com.jindo.minipay.settlement.repository;

import com.jindo.minipay.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

}
