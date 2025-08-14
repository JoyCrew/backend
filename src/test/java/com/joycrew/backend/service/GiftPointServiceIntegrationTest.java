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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GiftPointServiceIntegrationTest {

  @TestConfiguration
  static class TestConfig {
    @Bean
    TestRecognitionEventListener testRecognitionEventListener() {
      return new TestRecognitionEventListener();
    }
  }

  static class TestRecognitionEventListener implements ApplicationListener<RecognitionEvent> {
    private final java.util.List<RecognitionEvent> events =
        new java.util.concurrent.CopyOnWriteArrayList<>();

    @Override
    public void onApplicationEvent(RecognitionEvent event) {
      events.add(event);
    }

    public java.util.List<RecognitionEvent> getEvents() {
      return events;
    }
  }

  @Autowired
  private GiftPointService giftPointService;
  @Autowired
  private EmployeeRegistrationService registrationService;
  @Autowired
  private CompanyRepository companyRepository;
  @Autowired
  private WalletRepository walletRepository;
  @Autowired
  private TestRecognitionEventListener eventListener;

  private Long senderId, receiverId;
  private Company company;

  @BeforeEach
  void setUp() {
    company = companyRepository.save(Company.builder().companyName("Test Inc.").build());

    var senderRequest = new EmployeeRegistrationRequest(
        "Sender", "sender@test.com", "password123!",
        company.getCompanyName(), null, "Dev", AdminLevel.EMPLOYEE,
        null, null, null
    );
    var receiverRequest = new EmployeeRegistrationRequest(
        "Receiver", "receiver@test.com", "password123!",
        company.getCompanyName(), null, "Dev", AdminLevel.EMPLOYEE,
        null, null, null
    );

    senderId = registrationService.registerEmployee(senderRequest).getEmployeeId();
    receiverId = registrationService.registerEmployee(receiverRequest).getEmployeeId();

    // 초기 포인트 충전
    Wallet senderWallet = walletRepository.findByEmployee_EmployeeId(senderId).orElseThrow();
    senderWallet.addPoints(100);
    walletRepository.save(senderWallet);

    // 이전 테스트 이벤트가 섞이지 않도록 초기화
    eventListener.getEvents().clear();
  }

  @Test
  @DisplayName("[Integration] Gifting points should publish a RecognitionEvent")
  void giftPoints_ShouldPublishEvent() {
    // Given
    var request = new GiftPointRequest(receiverId, 50, "Event Test", List.of());

    // When
    giftPointService.giftPointsToColleague("sender@test.com", request);

    // Then
    long eventCount = eventListener.getEvents().stream()
        .filter(e -> e.getReceiverId().equals(receiverId) && e.getPoints() == 50)
        .count();

    assertThat(eventCount).isEqualTo(1);
  }
}