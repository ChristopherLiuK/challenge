package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.dto.TransferDTO;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private static final String ID_1 = "Id-1";
  private static final String ID_2 = "Id-2";

  private static TransferDTO transferDTO;

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();

    accountsService.createAccount(new Account(ID_1, new BigDecimal(1000)));
    accountsService.createAccount(new Account(ID_2, new BigDecimal(1000)));
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
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  @Test
  public void transfer() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(transferDTO)))
            .andExpect(status().isOk());
  }

  @Test
  public void transferFromNonExistentAccount() throws Exception {
    transferDTO.setAccountFromId("Id-0");
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(transferDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("One or both accounts entered don't exist"));
  }

  @Test
  public void transferToNonExistentAccount() throws Exception {
    transferDTO.setAccountToId("Id-0");
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(transferDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("One or both accounts entered don't exist"));
  }

  @Test
  public void transferOverdraft() throws Exception {
    transferDTO.setAmount(BigDecimal.valueOf(1500));
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(transferDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Insufficient Balance"));
  }

  @Test
  public void transferAmountEqualsToZero() throws Exception {
    transferDTO.setAmount(BigDecimal.ZERO);
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(transferDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Amount must be greater than 0"));
  }

  @Test
  public void transferAmountLessThanZero() throws Exception {
    transferDTO.setAmount(BigDecimal.valueOf(-1));
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(transferDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Amount must be greater than 0"));
  }

  @Test
  public void transferBetweenSameAccount() throws Exception {
    transferDTO.setAccountToId(transferDTO.getAccountFromId());
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(transferDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Sender and destinations accounts are the same"));
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
