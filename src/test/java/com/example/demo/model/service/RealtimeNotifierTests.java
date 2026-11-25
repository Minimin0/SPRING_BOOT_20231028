package com.example.demo.model.service;

import static org.mockito.Mockito.verify;

import com.example.demo.model.dto.Notification;
import com.example.demo.model.dto.SeatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class RealtimeNotifierTests {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private RealtimeNotifier notifier;

    @BeforeEach
    void setUp() {
        notifier = new RealtimeNotifier(messagingTemplate);
    }

    @Test
    void broadcastsSeatChanges() {
        SeatMessage message = new SeatMessage(1L, 2, 5, 3);

        notifier.sendSeats(message);

        verify(messagingTemplate).convertAndSend("/topic/seats", message);
    }

    @Test
    void sendsNotificationsOnlyToTheTargetUser() {
        Notification notification = new Notification("admin", "신청 알림", 1L);

        notifier.sendNotification(notification);

        verify(messagingTemplate).convertAndSendToUser(
                "admin", "/queue/notifications", notification);
    }
}
