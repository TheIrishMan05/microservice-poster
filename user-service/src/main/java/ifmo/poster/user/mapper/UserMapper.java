package ifmo.poster.user.mapper;

import ifmo.poster.contracts.user.UserDto;
import ifmo.poster.user.dto.CreateUserRequest;
import ifmo.poster.user.dto.UpdateUserRequest;
import ifmo.poster.user.dto.UserResponse;
import ifmo.poster.user.entity.User;

public final class UserMapper {

    private UserMapper() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static User toEntity(CreateUserRequest request) {
        User user = new User();
        user.setUserName(request.userName());
        user.setEmail(normalizeEmail(request.email()));
        user.setRole(request.role());
        return user;
    }

    public static void updateEntity(User user, UpdateUserRequest request) {
        user.setUserName(request.userName());
        user.setEmail(normalizeEmail(request.email()));
        user.setRole(request.role());
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUserName(), user.getEmail(), user.getRole());
    }

    public static UserDto toContract(User user) {
        return new UserDto(user.getId(), user.getUserName(), user.getEmail(), user.getRole());
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim();
    }
}
