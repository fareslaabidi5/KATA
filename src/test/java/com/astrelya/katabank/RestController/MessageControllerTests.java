package com.astrelya.katabank.RestController;

import com.astrelya.katabank.Entities.NotificationEntity;
import com.astrelya.katabank.Repositories.NotificationRepository;
import com.astrelya.katabank.RestControllers.MessageController;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageControllerTests {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private MessageController messageController;

    MessageControllerTests() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllMessages() {
        NotificationEntity notification = new NotificationEntity();
        notification.setId(1L);
        notification.setMessage("Test message");
        when(notificationRepository.findAll()).thenReturn(Collections.singletonList(notification));

        ResponseEntity<List<NotificationEntity>> response = messageController.getAllMessages();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetMessageById() {
        NotificationEntity notification = new NotificationEntity();
        notification.setId(1L);
        notification.setMessage("Test message");
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        ResponseEntity<NotificationEntity> response = messageController.getMessageById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Test message", response.getBody().getMessage());
    }

    @Test
    void testSendMessage() {
        ResponseEntity<String> response = messageController.sendMessage("Test message");

        assertEquals(202, response.getStatusCode().value());
        verify(jmsTemplate, times(1)).send(eq("NOTIFICATION.Q"), any());
    }
}