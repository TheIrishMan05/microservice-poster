package ifmo.poster.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotNull
        Long userId,

        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 1000)
        String message
) {
}
