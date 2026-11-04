package ifmo.poster.user.controller;

import ifmo.poster.contracts.user.UserDto;
import ifmo.poster.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserDto>> getById(@PathVariable Long id) {
        return userService.getContractById(id).map(ResponseEntity::ok);
    }
}
