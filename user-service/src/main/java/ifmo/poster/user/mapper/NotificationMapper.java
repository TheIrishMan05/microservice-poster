package ifmo.poster.user.mapper;

import ifmo.poster.user.dto.CreateNotificationRequest;
import ifmo.poster.user.dto.NotificationResponse;
import ifmo.poster.user.dto.UpdateNotificationRequest;
import ifmo.poster.user.entity.Notification;
import ifmo.poster.user.entity.User;

public final class NotificationMapper {

    private NotificationMapper() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static Notification toEntity(CreateNotificationRequest request, User user) {
        return new Notification(user, request.title().trim(), request.message().trim());
    }

    public static void updateEntity(Notification notification, UpdateNotificationRequest request) {
        notification.setTitle(request.title().trim());
        notification.setMessage(request.message().trim());
        notification.setRead(request.read());
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUser().getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
