package com.joycrew.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "frontendUrlBase", "https://test.joycrew.co.kr");
    }

    @Test
    @DisplayName("[Unit] Send password reset email - Verify MailSender call")
    void sendPasswordResetEmail_Success() {
        // Given
        String toEmail = "test@joycrew.com";
        String token = "test-token";
        String expectedResetUrl = "https://test.joycrew.co.kr/reset-password?token=" + token;

        // When
        emailService.sendPasswordResetEmail(toEmail, token);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).contains(toEmail);
        assertThat(sentMessage.getSubject()).isEqualTo("[JoyCrew] Password Reset Instructions");
        assertThat(sentMessage.getText()).contains(expectedResetUrl);
    }
}