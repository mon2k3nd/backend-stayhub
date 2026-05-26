package com.stayhub.api.service;

import com.stayhub.api.entity.Notification;
import com.stayhub.api.entity.NotificationType;
import java.util.List;

public interface NotificationService {
    List<Notification> getByUser(Long userId);
    long countUnread(Long userId);
    void markRead(Long notificationId);
    void markAllRead(Long userId);
    void send(Long userId, String title, String body, NotificationType type, Long refId);
}
