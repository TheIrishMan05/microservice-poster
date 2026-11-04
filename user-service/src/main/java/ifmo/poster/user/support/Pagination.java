package ifmo.poster.user.support;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class Pagination {

    public static final int MAX_PAGE_SIZE = 50;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private Pagination() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static Pageable of(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Параметр page не может быть отрицательным");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Параметр size должен быть от 1 до " + MAX_PAGE_SIZE
            );
        }
        return PageRequest.of(page, size);
    }
}
