package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.dto.TransferDTO;
import com.db.awmd.challenge.exception.*;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  private static final String ID_1 = "Id-1";
  private static final String ID_2 = "Id-2";
  private static Account account1;
  private static Account account2;

  private static TransferDTO transferDTO;

  @MockBean
  private NotificationService notificationService;

  @Autowired
  private AccountsService accountsService;

  @Before
  public void setup() {
    account1 = new Account(ID_1, new BigDecimal(1000));
    account2 = new Account(ID_2, new BigDecimal(1000));
    accountsService.createAccount(account1);
    accountsService.createAccount(account2);
    transferDTO = TransferDTO.builder()
            .accountFromId(ID_1)
            .accountToId(ID_2)
            .amount(new BigDecimal(300))
            .build();
  }

  @After
  public void cleanup() {
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }

  @Test
  public void transfer() {

    accountsService.transfer(transferDTO);

    assertEquals(BigDecimal.valueOf(700), account1.getBalance());
    assertEquals(BigDecimal.valueOf(1300), account2.getBalance());
    verify(notificationService, times(1))
            .notifyAboutTransfer(eq(account1), anyString());
    verify(notificationService, times(1))
            .notifyAboutTransfer(eq(account2), anyString());
  }

  @Test(expected = OverdraftException.class)
  public void transfer_overdraft(){
    transferDTO.setAmount(BigDecimal.valueOf(1500));
    accountsService.transfer(transferDTO);
  }

  @Test public void transfer_concurrent() throws InterruptedException {

    ExecutorService executorService = Executors.newFixedThreadPool(10);
    CountDownLatch countDownLatch = new CountDownLatch(99);
    IntStream.range(1,100).forEach(i -> executorService.execute(() -> {
      if(i % 2 == 0)
        accountsService.transfer(TransferDTO.builder()
                .accountFromId(ID_1)
                .accountToId(ID_2)
                .amount(BigDecimal.valueOf(20))
                .build());
      else
        accountsService.transfer(TransferDTO.builder()
                .accountFromId(ID_2)
                .accountToId(ID_1)
                .amount(BigDecimal.valueOf(20))
                .build());
      countDownLatch.countDown();
    }));
    countDownLatch.await();
    assertEquals(BigDecimal.valueOf(1020), account1.getBalance());
    assertEquals(BigDecimal.valueOf(980), account2.getBalance());
  }




  @Test(expected = NonExistentAccountException.class)
  public void checkTransfer_failsOnNonExistentAccount() {
    transferDTO.setAccountFromId("ID");
    accountsService.checkTransfer(transferDTO);
  }

  @Test(expected = SameAccountTransferException.class)
  public void checkTransfer_failsOnSameAccountTransfer(){
    transferDTO.setAccountToId(transferDTO.getAccountFromId());
    accountsService.checkTransfer(transferDTO);
  }

  @Test(expected =  InvalidAmountException.class)
  public void checkTransfer_failsOnAmountEqualsToZero(){
    transferDTO.setAmount(BigDecimal.ZERO);
    accountsService.checkTransfer(transferDTO);
  }

  @Test(expected =  InvalidAmountException.class)
  public void checkTransfer_failsOnAmountLessThanZero(){
    transferDTO.setAmount(BigDecimal.valueOf(-1));
    accountsService.checkTransfer(transferDTO);
  }

}
