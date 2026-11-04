package ifmo.poster.contracts.user;

import ifmo.poster.contracts.enums.Role;

public record UserDto(
        Long id,
        String userName,
        String email,
        Role role
) {
}
