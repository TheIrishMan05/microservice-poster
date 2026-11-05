package ifmo.poster.user.controller;

import ifmo.poster.user.dto.BulkCreateUsersRequest;
import ifmo.poster.user.dto.CreateUserRequest;
import ifmo.poster.user.dto.UpdateUserRequest;
import ifmo.poster.user.dto.UserResponse;
import ifmo.poster.user.service.UserService;
import ifmo.poster.contracts.support.Pagination;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public Mono<ResponseEntity<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request)
                .flatMap(body -> Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(body)));
    }

    @PostMapping("/bulk")
    public Mono<ResponseEntity<List<UserResponse>>> createBulk(
            @Valid @RequestBody BulkCreateUsersRequest request
    ) {
        return userService.createBulk(request.users())
                .flatMap(body -> Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(body)));
    }

    @DeleteMapping("/data")
    public Mono<ResponseEntity<Void>> clearAllData() {
        return userService.clearAllData()
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> getById(@PathVariable Long id) {
        return userService.getById(id)
                .flatMap(body -> Mono.just(ResponseEntity.ok(body)));
    }

    @GetMapping
    public Mono<ResponseEntity<List<UserResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int size
    ) {
        return userService.findAll(page, size)
                .flatMap(result -> Mono.just(toPagedResponse(result)));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.update(id, request)
                .flatMap(body -> Mono.just(ResponseEntity.ok(body)));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return userService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    private ResponseEntity<List<UserResponse>> toPagedResponse(Page<UserResponse> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(Pagination.TOTAL_COUNT_HEADER, String.valueOf(page.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
