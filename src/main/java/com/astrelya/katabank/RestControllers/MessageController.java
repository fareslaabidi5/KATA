package com.astrelya.katabank.RestControllers;

import com.astrelya.katabank.Entities.NotificationEntity;
import com.astrelya.katabank.Repositories.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final NotificationRepository notificationRepository;
    private final JmsTemplate jmsTemplate;

    private static final String QUEUE_NAME = "NOTIFICATION.Q";

    public MessageController(NotificationRepository notificationRepository, JmsTemplate jmsTemplate) {
        this.notificationRepository = notificationRepository;
        this.jmsTemplate = jmsTemplate;
    }

    @GetMapping
    public ResponseEntity<List<NotificationEntity>> getAllMessages() {
        List<NotificationEntity> messages = notificationRepository.findAll();
        log.info("Fetched {} notifications", messages.size());
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationEntity> getMessageById(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody String messageText) {
        String correlationId = UUID.randomUUID().toString();

        jmsTemplate.send(QUEUE_NAME, session -> {
            var textMessage = session.createTextMessage(messageText);
            textMessage.setJMSCorrelationID(correlationId);
            return textMessage;
        });

        log.info("Sent message to {} with correlationId: {}", QUEUE_NAME, correlationId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("Message sent with correlationId: " + correlationId);
    }

    // How many messages are currently sitting unconsumed on the queue
    @GetMapping("/queue-status")
    public ResponseEntity<Map<String, Object>> getQueueStatus() {
        int depth = jmsTemplate.browse(QUEUE_NAME, (session, browser) -> {
            int count = 0;
            Enumeration<?> messages = browser.getEnumeration();
            while (messages.hasMoreElements()) {
                messages.nextElement();
                count++;
            }
            return count;
        });

        log.info("Queue {} currently has {} message(s) pending", QUEUE_NAME, depth);

        return ResponseEntity.ok(Map.of(
                "queue", QUEUE_NAME,
                "pendingMessages", depth
        ));
    }

    // Peek at pending message contents without consuming them
    @GetMapping("/queue-status/preview")
    public ResponseEntity<List<Map<String, String>>> previewQueueMessages() {
        List<Map<String, String>> preview = jmsTemplate.browse(QUEUE_NAME, (session, browser) -> {
            List<Map<String, String>> result = new ArrayList<>();
            Enumeration<?> messages = browser.getEnumeration();
            while (messages.hasMoreElements()) {
                Message msg = (Message) messages.nextElement();
                String body = (msg instanceof TextMessage tm) ? tm.getText() : "(non-text message)";
                result.add(Map.of(
                        "correlationId", msg.getJMSCorrelationID() != null ? msg.getJMSCorrelationID() : "",
                        "body", body
                ));
            }
            return result;
        });

        return ResponseEntity.ok(preview);
    }

    // Total notifications actually persisted to the DB so far
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalSaved = notificationRepository.count();
        return ResponseEntity.ok(Map.of(
                "totalNotificationsSaved", totalSaved
        ));
    }
}