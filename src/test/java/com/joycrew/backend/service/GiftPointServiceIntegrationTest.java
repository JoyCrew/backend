package com.joycrew.backend.service;

import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.dto.GiftPointRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.event.RecognitionEvent;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RecordApplicationEvents
class GiftPointServiceIntegrationTest {

    @Autowired private GiftPointService giftPointService;
    @Autowired private EmployeeRegistrationService registrationService;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private ApplicationEvents applicationEvents;

    private Long senderId, receiverId;
    private Company company;

    @BeforeEach
    void setUp() {
        company = companyRepository.save(Company.builder().companyName("Test Inc.").build());
        var senderRequest = new EmployeeRegistrationRequest("Sender", "sender@test.com", "password123!", company.getCompanyName(), null, "Dev", AdminLevel.EMPLOYEE, null, null, null);
        var receiverRequest = new EmployeeRegistrationRequest("Receiver", "receiver@test.com", "password123!", company.getCompanyName(), null, "Dev", AdminLevel.EMPLOYEE, null, null, null);
        senderId = registrationService.registerEmployee(senderRequest).getEmployeeId();
        receiverId = registrationService.registerEmployee(receiverRequest).getEmployeeId();

        Wallet senderWallet = walletRepository.findByEmployee_EmployeeId(senderId).orElseThrow();
        senderWallet.addPoints(100);
        walletRepository.save(senderWallet);
    }

    @Test
    @DisplayName("[Integration] Gifting points should publish a RecognitionEvent")
    void giftPoints_ShouldPublishEvent() {
        // Given
        var request = new GiftPointRequest(receiverId, 50, "Event Test", List.of());

        // When
        giftPointService.giftPointsToColleague("sender@test.com", request);

        // Then
        long eventCount = applicationEvents.stream(RecognitionEvent.class)
                .filter(event -> event.getReceiverId().equals(receiverId) && event.getPoints() == 50)
                .count();
        assertThat(eventCount).isEqualTo(1);
    }
}