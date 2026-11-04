package ifmo.poster.user.dto;

import ifmo.poster.contracts.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Size(min = 5, max = 50)
        String userName,

        @Email
        String email,

        @NotNull
        Role role
) {
}
