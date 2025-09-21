package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.DatabaseTest;
import io.github.kovalev.specificationhelper.domain.entity.User;
import io.github.kovalev.specificationhelper.domain.entity.User_;
import io.github.kovalev.specificationhelper.enums.NullHandling;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotTest extends DatabaseTest {

    @Test
    void notEqualsComparison() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // 1. Используем NotEqual
        val notEqualSpec = new NotEqual<User>(User_.USERNAME, user.getUsername());

        // 2. Используем Not + Equal
        val notWithEqualSpec = new Not<User>(new Equal<>(User_.USERNAME, user.getUsername()));

        // Проверяем, что обе спецификации дают одинаковый результат
        assertThat(userRepository.findOne(notEqualSpec)).isEmpty();
        assertThat(userRepository.findOne(notWithEqualSpec)).isEmpty();

        // Проверяем с другим значением
        val notEqualForOtherValue = new NotEqual<User>(User_.USERNAME, "otherUser");
        val notWithEqualForOtherValue = new Not<User>(new Equal<>(User_.USERNAME, "otherUser"));

        assertThat(userRepository.findOne(notEqualForOtherValue)).isPresent();
        assertThat(userRepository.findOne(notWithEqualForOtherValue)).isPresent();
    }

    @Test
    void notWithMultipleConditions() {
        User user1 = userGenerator.one();
        User user2 = userGenerator.one();

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user1);
            entityManager.persist(user2);
        });

        // NOT (username = "user1" AND email = "user1@example.com")
        val complexNotSpec = new Not<User>(
                new Equal<>(User_.USERNAME, user1.getUsername()),
                new Equal<>(User_.EMAIL, user1.getEmail())
        );

        List<User> result = userRepository.findAll(complexNotSpec);
        assertThat(result)
                .hasSize(1)
                .extracting(User::getUsername)
                .containsExactly(user2.getUsername());
    }

    @Test
    void notWithNullHandling() {
        User user = userGenerator.one();
        user.setUsername(null);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // NOT (username = null)
        val notNullSpec = new Not<User>(new Equal<>(User_.USERNAME, null, NullHandling.USE_IS_NULL));

        // Должен вернуть запись, так как условие username != null не выполняется (username IS NULL)
        assertThat(userRepository.findOne(notNullSpec)).isEmpty();
    }

    @Test
    void notWithEmptyHandling() {
        int size = transactionalExecutor.executeWithInNewTransaction(() -> userGenerator.list(3).stream()
                .peek(entityManager::persist)
                .toList()
                .size());

        List<User> result = userRepository.findAll(new Not<>());

        assertThat(result).hasSize(size);
    }

    @Test
    void equivalenceWithNotEqual() {
        User user1 = userGenerator.one();
        User user2 = userGenerator.one();
        User user3 = userGenerator.one();
        user3.setUsername(null);

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user1);
            entityManager.persist(user2);
            entityManager.persist(user3);
        });

        checkEquivalenceForValue(user1.getUsername(), user2);
        checkEquivalenceForValue(user2.getUsername(), user1);
        checkEquivalenceForValue("nonExisting", user1, user2);
        checkEquivalenceForNullValue(NullHandling.USE_IS_NULL, user1, user2);
        checkEquivalenceForNullValue(NullHandling.IGNORE, user1, user2, user3);
    }

    private void checkEquivalenceForValue(Object value, User... expectedUsers) {
        // Создаем обе спецификации
        val notEqualSpec = new NotEqual<User>(User_.USERNAME, value);
        val notWithEqualSpec = new Not<User>(new Equal<>(User_.USERNAME, value));

        // Получаем результаты
        List<User> notEqualResult = userRepository.findAll(notEqualSpec);
        List<User> notWithEqualResult = userRepository.findAll(notWithEqualSpec);

        assertThat(notEqualResult)
                .as("NotEqual для значения '%s'", value)
                .containsExactlyInAnyOrder(expectedUsers);

        assertThat(notWithEqualResult)
                .as("Not+Equal для значения '%s'", value)
                .containsExactlyInAnyOrder(expectedUsers);
    }

    private void checkEquivalenceForNullValue(NullHandling nullHandling, User... expectedUsers) {
        val notEqualSpec = new NotEqual<User>(User_.USERNAME, null, nullHandling);
        val notWithEqualSpec = new Not<User>(new Equal<>(User_.USERNAME, null, nullHandling));

        List<User> notEqualResult = userRepository.findAll(notEqualSpec);
        List<User> notWithEqualResult = userRepository.findAll(notWithEqualSpec);

        assertThat(notEqualResult)
                .as("NotEqual для NULL значения")
                .containsExactlyInAnyOrder(expectedUsers);

        assertThat(notWithEqualResult)
                .as("Not+Equal для NULL значения")
                .containsExactlyInAnyOrder(expectedUsers);
    }
}