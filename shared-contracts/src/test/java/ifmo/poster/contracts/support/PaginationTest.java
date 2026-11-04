package ifmo.poster.contracts.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PaginationTest {

    @Test
    void validatesPageAndSize() {
        Pagination.validate(0, 20);
        assertThat(Pagination.offset(2, 20)).isEqualTo(40L);
        Pagination.validateLimit(50);
    }

    @Test
    void rejectsInvalidPaging() {
        assertThatThrownBy(() -> Pagination.validate(-1, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Pagination.validate(0, 51))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Pagination.validateLimit(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
