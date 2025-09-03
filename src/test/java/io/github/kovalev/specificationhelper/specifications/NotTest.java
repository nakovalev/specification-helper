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
        val notEqualSpec = new NotEqual<User>(user.getUsername(), User_.USERNAME);

        // 2. Используем Not + Equal
        val notWithEqualSpec = new Not<User>(new Equal<>(user.getUsername(), User_.USERNAME));

        // Проверяем, что обе спецификации дают одинаковый результат
        assertThat(userRepository.findOne(notEqualSpec)).isEmpty();
        assertThat(userRepository.findOne(notWithEqualSpec)).isEmpty();

        // Проверяем с другим значением
        val notEqualForOtherValue = new NotEqual<User>("otherUser", User_.USERNAME);
        val notWithEqualForOtherValue = new Not<User>(new Equal<>("otherUser", User_.USERNAME));

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
                new Equal<>(user1.getUsername(), User_.USERNAME),
                new Equal<>(user1.getEmail(), User_.EMAIL)
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
        val notNullSpec = new Not<User>(new Equal<>(null, NullHandling.USE_IS_NULL, User_.USERNAME));

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
        val notEqualSpec = new NotEqual<User>(value, User_.USERNAME);
        val notWithEqualSpec = new Not<User>(new Equal<>(value, User_.USERNAME));

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
        val notEqualSpec = new NotEqual<User>(null, nullHandling, User_.USERNAME);
        val notWithEqualSpec = new Not<User>(new Equal<>(null, nullHandling, User_.USERNAME));

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