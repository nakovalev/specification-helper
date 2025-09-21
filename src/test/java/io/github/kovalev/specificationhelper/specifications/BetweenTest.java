package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.DatabaseTest;
import io.github.kovalev.specificationhelper.domain.entity.ComparableEntity;
import io.github.kovalev.specificationhelper.domain.entity.ComparableEntity_;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BetweenTest extends DatabaseTest {

    @Test
    void integerBetween() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(5);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        // В пределах диапазона
        assertFound(new Between<>(ComparableEntity_.INT_VALUE, List.of(4, 6)));
        // Граничные значения
        assertFound(new Between<>(ComparableEntity_.INT_VALUE, List.of(5, 5)));
        assertFound(new Between<>(ComparableEntity_.INT_VALUE, List.of(4, 5)));
        assertFound(new Between<>(ComparableEntity_.INT_VALUE, List.of(5, 6)));
        // За пределами диапазона
        assertNotFound(new Between<>(ComparableEntity_.INT_VALUE, List.of(6, 8)));
        assertNotFound(new Between<>(ComparableEntity_.INT_VALUE, List.of(1, 4)));
    }

    @Test
    void bigDecimalBetween() {
        ComparableEntity entity = new ComparableEntity();
        entity.setBigDecimalValue(new BigDecimal("15.50"));
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new Between<>(ComparableEntity_.BIG_DECIMAL_VALUE, List.of(new BigDecimal("15.00"), new BigDecimal("16.00"))));
        assertFound(new Between<>(ComparableEntity_.BIG_DECIMAL_VALUE, List.of(new BigDecimal("15.50"), new BigDecimal("15.50"))));
        assertNotFound(new Between<>(ComparableEntity_.BIG_DECIMAL_VALUE, List.of(new BigDecimal("16.00"), new BigDecimal("17.00"))));
    }

    @Test
    void stringBetween() {
        ComparableEntity entity = new ComparableEntity();
        entity.setStringValue("Mango");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        // Лексикографическое сравнение
        assertFound(new Between<>(ComparableEntity_.STRING_VALUE, List.of("Apple", "Orange")));
        assertFound(new Between<>(ComparableEntity_.STRING_VALUE, List.of("Mango", "Mango")));
        assertNotFound(new Between<>(ComparableEntity_.STRING_VALUE, List.of("Aardvark", "Lemon")));
    }

    @Test
    void dateBetween() {
        LocalDate today = LocalDate.now();
        ComparableEntity entity = new ComparableEntity();
        entity.setDateValue(today);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new Between<>(ComparableEntity_.DATE_VALUE, List.of(today.minusDays(1), today.plusDays(1))));
        assertFound(new Between<>(ComparableEntity_.DATE_VALUE, List.of(today, today)));
        assertNotFound(new Between<>(ComparableEntity_.DATE_VALUE, List.of(today.plusDays(1), today.plusDays(2))));
    }

    @Test
    void partialNullRanges() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(10);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        // Только нижняя граница
        assertFound(new Between<>(ComparableEntity_.INT_VALUE, Arrays.asList(9, null)));
        // Только верхняя граница
        assertFound(new Between<>(ComparableEntity_.INT_VALUE, Arrays.asList(null, 11)));
        // Обе границы null
        List<Integer> nullBounds = Arrays.asList(null, null);
        assertFound(new Between<>(ComparableEntity_.INT_VALUE, nullBounds));
    }

    @Test
    void nullFieldHandling() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(null); // поле null
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        // Для null-поля between не должен находить запись
        assertNotFound(new Between<>(ComparableEntity_.INT_VALUE, List.of(1, 10)));
    }

    @Test
    void invalidParameters() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(5);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        // Слишком мало значений
        assertFound(new Between<>(ComparableEntity_.INT_VALUE, new ArrayList<Integer>()));
        // Все значения null
        List<Integer> nullBounds = Arrays.asList(null, null);
        assertFound(new Between<>(ComparableEntity_.INT_VALUE, nullBounds));
        // Неправильный порядок границ
        assertNotFound(new Between<>(ComparableEntity_.INT_VALUE, List.of(6, 4)));
    }

    private void assertFound(Specification<ComparableEntity> spec) {
        assertThat(comparableRepository.findOne(spec)).isPresent();
    }

    private void assertNotFound(Specification<ComparableEntity> spec) {
        assertThat(comparableRepository.findOne(spec)).isEmpty();
    }
}