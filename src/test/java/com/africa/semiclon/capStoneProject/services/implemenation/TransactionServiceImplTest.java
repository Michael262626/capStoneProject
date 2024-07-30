package com.africa.semiclon.capStoneProject.services.implemenation;

import com.africa.semiclon.capStoneProject.data.models.PricingPlanType;
import com.africa.semiclon.capStoneProject.data.models.Transaction;
import com.africa.semiclon.capStoneProject.data.models.User;
import com.africa.semiclon.capStoneProject.data.repository.AdminRepository;
import com.africa.semiclon.capStoneProject.data.repository.TransactionRepository;
import com.africa.semiclon.capStoneProject.data.repository.UserRepository;
import com.africa.semiclon.capStoneProject.dtos.request.CreatePlanRequest;
import com.africa.semiclon.capStoneProject.dtos.request.GetBalanceRequest;
import com.africa.semiclon.capStoneProject.dtos.request.PaymentRequest;
import com.africa.semiclon.capStoneProject.dtos.request.WithdrawRequest;
import com.africa.semiclon.capStoneProject.dtos.response.CreatePlanResponse;
import com.africa.semiclon.capStoneProject.dtos.response.InitializePaymentResponse;
import com.africa.semiclon.capStoneProject.services.interfaces.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;


import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.modelmapper.internal.bytebuddy.matcher.ElementMatchers.any;

@SpringBootTest
@Sql(scripts = "/db/data.sql")
public class TransactionServiceImplTest {

    @Autowired
    private TransactionServiceImpl transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    public void testMakePaymentToUser() {
        PaymentRequest request = new PaymentRequest();
        request.setAdminId(300L);
        request.setUserId(10L);
        request.setAmount(BigDecimal.valueOf(100.00));

        CreatePlanResponse createPlanResponse = new CreatePlanResponse();
        createPlanResponse.setStatus(true);
        createPlanResponse.setData(new CreatePlanResponse.Data());

        CreatePlanResponse response = transactionService.makePaymentToUser(request);

        assertNotNull(response);
        assertTrue(response.getStatus());

        Transaction transaction = transactionRepository.findAll().get(0);
        assertNotNull(transaction);
        assertEquals(PricingPlanType.PAYMENT, transaction.getPlanType());
        assertEquals(BigDecimal.valueOf(100.00), transaction.getAmount());
    }

    @Test
    public void testProcessWithdrawal() {
        User user = userRepository.findById(10L).orElseThrow();
        user.setBalance(BigDecimal.valueOf(500.00));
        userRepository.save(user);

        WithdrawRequest request = new WithdrawRequest();
        request.setUserId(10L);
        request.setAmount(BigDecimal.valueOf(100.00));

        InitializePaymentResponse initializePaymentResponse = new InitializePaymentResponse();
        initializePaymentResponse.setStatus(true);
        initializePaymentResponse.setData(new InitializePaymentResponse.Data("pay_67890", "100.00", "NGN"));


        transactionService.processWithdrawal(request);

        Transaction transaction = transactionRepository.findAll().getFirst();
        assertNotNull(transaction);
        assertEquals(PricingPlanType.WITHDRAWAL, transaction.getPlanType());
        assertEquals(BigDecimal.valueOf(100.00), transaction.getAmount());

        User updatedUser = userRepository.findById(10L).orElseThrow();
        assertEquals(BigDecimal.valueOf(400.00), updatedUser.getBalance());
    }
}