package com.jindo.minipay.member.event;

import static org.springframework.transaction.event.TransactionPhase.BEFORE_COMMIT;

import com.jindo.minipay.account.checking.service.CheckingAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSignupEventListener {

  private final CheckingAccountService checkingAccountService;

  @TransactionalEventListener(phase = BEFORE_COMMIT)
  public void createCheckingAccount(MemberSignupEvent event) {
    checkingAccountService.create(event.getMemberId());
  }
}
