package io.github.kovalev.specificationhelper.specifications;

import io.github.kovalev.specificationhelper.DatabaseTest;
import io.github.kovalev.specificationhelper.domain.entity.ComparableEntity;
import io.github.kovalev.specificationhelper.domain.entity.ComparableEntity_;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LessThanTest extends DatabaseTest {

    @Test
    void integerComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(10);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new LessThan<>(ComparableEntity_.INT_VALUE, 11));
        assertNotFound(new LessThan<>(ComparableEntity_.INT_VALUE, 10));
        assertNotFound(new LessThan<>(ComparableEntity_.INT_VALUE, 9));
    }

    @Test
    void bigDecimalComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setBigDecimalValue(new BigDecimal("15.7500"));
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new LessThan<>(ComparableEntity_.BIG_DECIMAL_VALUE, new BigDecimal("15.7501")));
        assertNotFound(new LessThan<>(ComparableEntity_.BIG_DECIMAL_VALUE, new BigDecimal("15.75")));
        assertNotFound(new LessThan<>(ComparableEntity_.BIG_DECIMAL_VALUE, new BigDecimal("15.7499")));
    }

    @Test
    void stringComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setStringValue("Hello");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new LessThan<>(ComparableEntity_.STRING_VALUE, "Hellz"));
        assertNotFound(new LessThan<>(ComparableEntity_.STRING_VALUE, "Hello"));
        assertNotFound(new LessThan<>(ComparableEntity_.STRING_VALUE, "Hell"));
    }

    @Test
    void charComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setCharValue('M');
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new LessThan<>(ComparableEntity_.CHAR_VALUE, 'N'));
        assertNotFound(new LessThan<>(ComparableEntity_.CHAR_VALUE, 'M'));
        assertNotFound(new LessThan<>(ComparableEntity_.CHAR_VALUE, 'L'));
    }

    @Test
    void dateComparison() {
        LocalDate today = LocalDate.now();
        ComparableEntity entity = new ComparableEntity();
        entity.setDateValue(today);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new LessThan<>(ComparableEntity_.DATE_VALUE, today.plusDays(1)));
        assertNotFound(new LessThan<>(ComparableEntity_.DATE_VALUE, today));
        assertNotFound(new LessThan<>(ComparableEntity_.DATE_VALUE, today.minusDays(1)));
    }

    @Test
    void datetimeComparison() {
        LocalDateTime now = LocalDateTime.now();
        ComparableEntity entity = new ComparableEntity();
        entity.setDatetimeValue(now);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new LessThan<>(ComparableEntity_.DATETIME_VALUE, now.plusMinutes(1)));
        assertNotFound(new LessThan<>(ComparableEntity_.DATETIME_VALUE, now));
        assertNotFound(new LessThan<>(ComparableEntity_.DATETIME_VALUE, now.minusMinutes(1)));
    }

    @Test
    void multipleFieldsComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(5);
        entity.setStringValue("Text");
        entity.setDateValue(LocalDate.of(2023, 6, 15));
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new LessThan<>(ComparableEntity_.INT_VALUE, 6));
        assertFound(new LessThan<>(ComparableEntity_.STRING_VALUE, "Texu"));
        assertFound(new LessThan<>(ComparableEntity_.DATE_VALUE, LocalDate.of(2023, 6, 16)));
    }

    @Test
    void nullValueHandling() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(1);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new LessThan<>(ComparableEntity_.INT_VALUE, null));
        assertNotFound(new LessThan<>(ComparableEntity_.LONG_VALUE, 0L));
    }

    private void assertFound(Specification<ComparableEntity> spec) {
        assertThat(comparableRepository.findOne(spec)).isPresent();
    }

    private void assertNotFound(Specification<ComparableEntity> spec) {
        assertThat(comparableRepository.findOne(spec)).isEmpty();
    }
}