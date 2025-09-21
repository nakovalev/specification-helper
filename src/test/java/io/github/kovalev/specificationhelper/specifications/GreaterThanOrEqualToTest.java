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

class GreaterThanOrEqualToTest extends DatabaseTest {

    @Test
    void integerComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(10);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.INT_VALUE, 9));
        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.INT_VALUE,10));
        assertNotFound(new GreaterThanOrEqualTo<>(ComparableEntity_.INT_VALUE, 11));
    }

    @Test
    void bigDecimalComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setBigDecimalValue(new BigDecimal("15.7500"));
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.BIG_DECIMAL_VALUE ,new BigDecimal("15.7499")));
        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.BIG_DECIMAL_VALUE, new BigDecimal("15.75")));
        assertNotFound(new GreaterThanOrEqualTo<>(ComparableEntity_.BIG_DECIMAL_VALUE, new BigDecimal("15.7501")));
    }

    @Test
    void stringComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setStringValue("Hello");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.STRING_VALUE, "Hell"));
        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.STRING_VALUE, "Hello"));
        assertNotFound(new GreaterThanOrEqualTo<>(ComparableEntity_.STRING_VALUE, "Hellz"));
    }

    @Test
    void charComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setCharValue('M');
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.CHAR_VALUE, 'L'));
        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.CHAR_VALUE, 'M'));
        assertNotFound(new GreaterThanOrEqualTo<>(ComparableEntity_.CHAR_VALUE, 'N'));
    }

    @Test
    void dateComparison() {
        LocalDate today = LocalDate.now();
        ComparableEntity entity = new ComparableEntity();
        entity.setDateValue(today);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.DATE_VALUE, today.minusDays(1)));
        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.DATE_VALUE, today));
        assertNotFound(new GreaterThanOrEqualTo<>(ComparableEntity_.DATE_VALUE, today.plusDays(1)));
    }

    @Test
    void datetimeComparison() {
        LocalDateTime now = LocalDateTime.now();
        ComparableEntity entity = new ComparableEntity();
        entity.setDatetimeValue(now);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.DATETIME_VALUE, now.minusMinutes(1)));
        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.DATETIME_VALUE, now));
        assertNotFound(new GreaterThanOrEqualTo<>(ComparableEntity_.DATETIME_VALUE, now.plusMinutes(1)));
    }

    @Test
    void multipleFieldsComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(5);
        entity.setStringValue("Text");
        entity.setDateValue(LocalDate.of(2023, 6, 15));
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.INT_VALUE, 4));
        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.STRING_VALUE, "Tex"));
        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.DATE_VALUE, LocalDate.of(2023, 6, 14)));
    }

    @Test
    void nullValueHandling() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(1);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(ComparableEntity_.INT_VALUE, null));
        assertNotFound(new GreaterThanOrEqualTo<>(ComparableEntity_.LONG_VALUE, 2L));
    }

    private void assertFound(Specification<ComparableEntity> spec) {
        assertThat(comparableRepository.findOne(spec)).isPresent();
    }

    private void assertNotFound(Specification<ComparableEntity> spec) {
        assertThat(comparableRepository.findOne(spec)).isEmpty();
    }
}