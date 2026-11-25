package com.example.demo.model.service;

import com.example.demo.model.dto.Notification;
import com.example.demo.model.dto.SeatMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RealtimeNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener
    public void sendSeats(SeatMessage message) {
        messagingTemplate.convertAndSend("/topic/seats", message);
    }

    @TransactionalEventListener
    public void sendNotification(Notification notification) {
        messagingTemplate.convertAndSendToUser(
                notification.to(), "/queue/notifications", notification);
    }
}
