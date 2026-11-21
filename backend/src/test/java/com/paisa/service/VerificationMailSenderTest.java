package com.paisa.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;

class VerificationMailSenderTest {
  @Test
  void recoveryEmailIsClearlySeparateFromSignupVerification() {
    @SuppressWarnings("unchecked")
    ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
    JavaMailSender transport = mock(JavaMailSender.class);
    when(provider.getIfAvailable()).thenReturn(transport);
    when(provider.getObject()).thenReturn(transport);
    VerificationMailSender mail =
        new VerificationMailSender(provider, "smtp.example.test", "sender@example.test");
    mail.sendResetCode("recipient@example.test", "123456");
    var message = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(transport).send(message.capture());
    assertThat(message.getValue().getSubject()).isEqualTo("Reset your Paisa password");
    assertThat(message.getValue().getText())
        .contains("password reset code", "123456", "password has not changed");
  }

  @Test
  void missingSenderFailsClosed() {
    @SuppressWarnings("unchecked")
    ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
    VerificationMailSender mail = new VerificationMailSender(provider, "", "");
    assertThat(mail.configured()).isFalse();
    assertThatThrownBy(() -> mail.sendCode("test@example.test", "123456"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("not connected");
    verifyNoInteractions(provider);
  }

  @Test
  void verificationEmailContainsTheOneTimeCodeAndCorrectRecipient() {
    @SuppressWarnings("unchecked")
    ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
    JavaMailSender transport = mock(JavaMailSender.class);
    when(provider.getIfAvailable()).thenReturn(transport);
    when(provider.getObject()).thenReturn(transport);
    VerificationMailSender mail =
        new VerificationMailSender(provider, "smtp.example.test", "sender@example.test");
    mail.sendCode("recipient@example.test", "123456");
    org.mockito.ArgumentCaptor<SimpleMailMessage> message =
        org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(transport).send(message.capture());
    assertThat(message.getValue().getTo()).containsExactly("recipient@example.test");
    assertThat(message.getValue().getFrom()).isEqualTo("sender@example.test");
    assertThat(message.getValue().getText()).contains("123456", "10 minutes");
  }

  @Test
  void providerFailureDoesNotExposeProviderOrCredentialDetails() {
    @SuppressWarnings("unchecked")
    ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
    JavaMailSender transport = mock(JavaMailSender.class);
    when(provider.getIfAvailable()).thenReturn(transport);
    when(provider.getObject()).thenReturn(transport);
    doThrow(new MailSendException("private provider details"))
        .when(transport)
        .send(any(SimpleMailMessage.class));
    VerificationMailSender mail =
        new VerificationMailSender(provider, "smtp.example.test", "sender@example.test");
    assertThatThrownBy(() -> mail.sendCode("recipient@example.test", "123456"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("could not send")
        .hasMessageNotContaining("private provider details")
        .hasMessageNotContaining("123456");
  }

  @Test
  void requestsAreLimitedByNetworkAddress() {
    VerificationRateLimiter limiter = new VerificationRateLimiter(2);
    limiter.consume("test-address");
    limiter.consume("test-address");
    assertThatThrownBy(() -> limiter.consume("test-address"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Too many");
    assertThatCode(() -> limiter.consume("another-test-address")).doesNotThrowAnyException();
  }
}
