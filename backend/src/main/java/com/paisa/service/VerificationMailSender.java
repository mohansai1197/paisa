package com.paisa.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class VerificationMailSender {
  private final ObjectProvider<JavaMailSender> sender;
  private final String host, from;

  public VerificationMailSender(
      ObjectProvider<JavaMailSender> sender,
      @Value("${spring.mail.host:}") String host,
      @Value("${paisa.mail-from:}") String from) {
    this.sender = sender;
    this.host = host;
    this.from = from;
  }

  public boolean configured() {
    return !host.isBlank() && !from.isBlank() && sender.getIfAvailable() != null;
  }

  public void requireConfigured() {
    if (!configured())
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "Email verification is not connected yet. Account creation is unavailable until an email sender is configured.");
  }

  public void sendCode(String email, String code) {
    send(
        email,
        "Verify your Paisa email address",
        "Your Paisa verification code is: "
            + code
            + "\n\nThis code expires in 10 minutes and can be used once. Enter it only in Paisa."
            + "\nIf you did not request this, ignore this email. Your account will remain unverified.");
  }

  public void sendResetCode(String email, String code) {
    send(
        email,
        "Reset your Paisa password",
        "Your Paisa password reset code is: "
            + code
            + "\n\nThis code expires in 10 minutes and can be used once. Enter it only in Paisa."
            + "\nIf you did not request a password reset, ignore this email. Your password has not changed.");
  }

  private void send(String email, String subject, String text) {
    requireConfigured();
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(email);
    message.setSubject(subject);
    message.setText(text);
    try {
      sender.getObject().send(message);
    } catch (org.springframework.mail.MailException e) {
      // Never log credentials, recipient addresses, provider responses, or verification codes.
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "We could not send the email. Please try again later.");
    }
  }
}
