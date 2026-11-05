package ifmo.poster.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkCreateUsersRequest(
        @NotEmpty List<@Valid CreateUserRequest> users
) {
}
