package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.dto.TransferDTO;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.exception.NonExistentAccountException;
import com.db.awmd.challenge.exception.SameAccountTransferException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  private final NotificationService notificationService;

  public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public void transfer(TransferDTO transferDTO){
    accountsRepository.updateBalance(transferDTO.getAccountFromId(), transferDTO.getAmount().negate());
    accountsRepository.updateBalance(transferDTO.getAccountToId(), transferDTO.getAmount());
    notificationService.notifyAboutTransfer(accountsRepository.getAccount(transferDTO.getAccountFromId()),
            "Transferred ".concat(transferDTO.getAmount().toString()).concat(" to account ").concat(transferDTO.getAccountToId()));
    notificationService.notifyAboutTransfer(accountsRepository.getAccount(transferDTO.getAccountToId()),
            "Received ".concat(transferDTO.getAmount().toString()).concat(" from account").concat(transferDTO.getAccountFromId()));
  }

  public void checkTransfer(TransferDTO transferDTO){
    if(Objects.isNull(accountsRepository.getAccount(transferDTO.getAccountFromId())) || Objects.isNull(accountsRepository.getAccount(transferDTO.getAccountToId())))
      throw new NonExistentAccountException();
    if (transferDTO.getAccountFromId().equals(transferDTO.getAccountToId()))
      throw new SameAccountTransferException();
    if(transferDTO.getAmount().signum() <= 0)
      throw new InvalidAmountException();
  }
}
