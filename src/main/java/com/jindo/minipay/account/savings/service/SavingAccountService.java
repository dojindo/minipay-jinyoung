package com.jindo.minipay.account.savings.service;

import static com.jindo.minipay.account.common.type.AccountType.SAVING;
import static com.jindo.minipay.global.exception.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.jindo.minipay.global.exception.ErrorCode.MEMBER_NOT_FOUND;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.util.AccountNumberCreator;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateRequest;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateResponse;
import com.jindo.minipay.account.savings.dto.SavingAccountDepositRequest;
import com.jindo.minipay.account.savings.dto.SavingAccountDepositResponse;
import com.jindo.minipay.account.savings.entity.SavingAccount;
import com.jindo.minipay.account.savings.repository.SavingAccountRepository;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.global.exception.ErrorCode;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavingAccountService {

  private final SavingAccountRepository savingAccountRepository;
  private final MemberRepository memberRepository;
  private final AccountNumberCreator accountNumberCreator;
  private final CheckingAccountRepository checkingAccountRepository;

  public SavingAccountCreateResponse create(SavingAccountCreateRequest request) {
    Member owner = memberRepository.findById(request.getMemberId())
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    String accountNumber = accountNumberCreator.create(SAVING);

    SavingAccount savedSavingAccount = savingAccountRepository.save(
        SavingAccount.of(owner, accountNumber));

    return new SavingAccountCreateResponse(savedSavingAccount.getId());
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public SavingAccountDepositResponse deposit(SavingAccountDepositRequest request) {

    SavingAccount savingAccount = savingAccountRepository.findByIdForUpdate(
            request.getSavingAccountId())
        .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));

    validateExistsMember(request.getOwnerId());

    CheckingAccount checkingAccount = checkingAccountRepository.findByOwnerIdForUpdate(
            request.getOwnerId())
        .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));

    validateEnoughBalance(checkingAccount.getBalance(), request.getAmount());

    savingAccount.deposit(request.getAmount());
    checkingAccount.withdraw(request.getAmount());

    return SavingAccountDepositResponse.fromEntity(savingAccount);
  }

  private void validateExistsMember(Long ownerId) {
    if (!memberRepository.existsById(ownerId)) {
      throw new CustomException(MEMBER_NOT_FOUND);
    }
  }

  private void validateEnoughBalance(long balance, long amount) {
    if (balance < amount) {
      throw new CustomException(ErrorCode.BALANCE_NOT_ENOUGH);
    }
  }
}
