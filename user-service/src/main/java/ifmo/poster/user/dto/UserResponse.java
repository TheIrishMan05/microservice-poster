package ifmo.poster.user.dto;

import ifmo.poster.contracts.enums.Role;

public record UserResponse(
        Long id,
        String userName,
        String email,
        Role role
) {
}
