package com.astrelya.katabank.utils;

import com.astrelya.katabank.Entities.NotificationEntity;
import com.astrelya.katabank.Repositories.NotificationRepository;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class NotificationListenerTests {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TextMessage textMessage;

    @InjectMocks
    private NotificationListener notificationListener;

    NotificationListenerTests() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReceiveMessage() throws Exception {
        when(textMessage.getText()).thenReturn("Test message");
        when(textMessage.getJMSCorrelationID()).thenReturn("12345");

        notificationListener.receiveMessage(textMessage);

        verify(notificationRepository, times(1)).save(any(NotificationEntity.class));
    }
}