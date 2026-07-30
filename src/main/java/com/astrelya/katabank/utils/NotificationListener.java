package com.astrelya.katabank.utils;

import com.astrelya.katabank.Entities.NotificationEntity;
import com.astrelya.katabank.Repositories.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;

@Slf4j
@Component
public class NotificationListener {

    @Autowired
    private NotificationRepository notificationRepository;

    @Retryable(
            value = { RuntimeException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @JmsListener(destination = "NOTIFICATION.Q")
    public void receiveMessage(TextMessage textMessage) {
        try {
            log.info("Received message: {}", textMessage.getText());

            NotificationEntity notification = new NotificationEntity();
            notification.setMessage(textMessage.getText());
            notification.setCorrelationId(textMessage.getJMSCorrelationID());

            notificationRepository.save(notification);

            log.info("Saved notification with correlationId: {}", notification.getCorrelationId());

        } catch (JMSException e) {
            log.error("Error reading JMS message: {}", e.getMessage(), e);
            throw new RuntimeException(e); // triggers retry
        } catch (Exception e) {
            log.error("Unexpected error saving notification: {}", e.getMessage(), e);
            throw new RuntimeException(e); // triggers retry
        }
    }
}