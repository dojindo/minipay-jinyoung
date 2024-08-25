package com.jindo.minipay.settlement.repository;

import com.jindo.minipay.settlement.entity.Settlement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

  List<Settlement> findByRequesterId(Long requesterId);

}
