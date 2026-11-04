package ifmo.poster.contracts.support;

public final class Pagination {

    public static final int MAX_PAGE_SIZE = 50;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String TOTAL_COUNT_HEADER = "X-Total-Count";

    private Pagination() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static void validate(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Параметр page не может быть отрицательным");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Параметр size должен быть от 1 до " + MAX_PAGE_SIZE);
        }
    }

    public static void validateLimit(int limit) {
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Параметр limit должен быть от 1 до " + MAX_PAGE_SIZE);
        }
    }

    public static long offset(int page, int size) {
        validate(page, size);
        return (long) page * size;
    }
}
