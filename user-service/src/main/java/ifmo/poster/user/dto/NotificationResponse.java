package ifmo.poster.user.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        String title,
        String message,
        boolean read,
        LocalDateTime createdAt
) {
}
