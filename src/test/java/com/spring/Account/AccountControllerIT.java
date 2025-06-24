package com.spring.Account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.Account.model.Account;
import com.spring.Account.service.AccountService;
import com.spring.Account.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private EmailService emailService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        accountService.deleteAll();

        testAccount = new Account();
        testAccount.setId(UUID.randomUUID());
        testAccount.setFirstName("John");
        testAccount.setLastName("Doe");
        testAccount.setEmail("john.doe@example.com");
        testAccount.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createAccount_ValidData_ShouldReturnCreatedAccount() throws Exception {
        mockMvc.perform(post("/api/account")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(testAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value(testAccount.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(testAccount.getLastName()))
                .andExpect(jsonPath("$.email").value(testAccount.getEmail()));

        verify(emailService, times(1)).sendAccountCreatedEmail(
                anyString(), anyString(), any(UUID.class));
    }

    @Test
    void createAccount_InvalidData_ShouldReturnBadRequest() throws Exception {
        testAccount.setFirstName("A");
        testAccount.setLastName("");
        testAccount.setEmail("invalid-email");

        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(testAccount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("First name must be at least 2 characters"))
                .andExpect(jsonPath("$.lastName").value("Last name is required"))
                .andExpect(jsonPath("$.email").value("Invalid email"));

        verify(emailService, never()).sendAccountCreatedEmail(any(), any(), any());
    }

    @Test
    void getAccount_ValidId_ShouldReturnAccount() throws Exception {
        accountService.save(testAccount);

        mockMvc.perform(get("/api/account/{id}", testAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAccount.getId().toString()))
                .andExpect(jsonPath("$.firstName").value(testAccount.getFirstName()));
    }

    @Test
    void getAccount_withInvalidId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/account/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
