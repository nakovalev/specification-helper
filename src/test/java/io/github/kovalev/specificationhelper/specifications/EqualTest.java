package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.DatabaseTest;
import io.github.kovalev.specificationhelper.domain.entity.User;
import io.github.kovalev.specificationhelper.domain.entity.User_;
import io.github.kovalev.specificationhelper.enums.NullHandling;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EqualTest extends DatabaseTest {

    @Test
    void equalWithNonNullValues() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new Equal<>(User_.ID, user.getId()))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME, user.getUsername()))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(User_.EMAIL, user.getEmail()))).isPresent();

        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME,"nonexistent"))).isEmpty();
    }

    @Test
    void caseSensitiveComparison() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME, "TestUser"))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME, "testuser"))).isEmpty();
        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME, "TESTUSER"))).isEmpty();
    }

    @Test
    void caseInsensitiveComparison() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME, "TestUser", true))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME, "testuser", true))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME, "TESTUSER", true))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME, "different", true))).isEmpty();
    }

    @Test
    void ignoreNullValue() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));
        assertThat(userRepository.findOne(new Equal<>(User_.ID, null))).isPresent();
    }

    @Test
    void addNullAsIsNull() {
        User userWithNullEmail = userGenerator.one();
        userWithNullEmail.setEmail(null);
        User userWithEmail = userGenerator.one();

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(userWithNullEmail);
            entityManager.persist(userWithEmail);
        });

        val emailIsNull = new Equal<User>(User_.EMAIL, null, NullHandling.USE_IS_NULL);
        assertThat(userRepository.findAll(emailIsNull)).hasSize(1).allMatch(u -> u.getEmail() == null);

        val usernameEqual = new Equal<User>(User_.USERNAME, "test", NullHandling.USE_IS_NULL);
        assertThat(userRepository.findOne(usernameEqual)).isEmpty();
    }

    @Test
    void nonStringValueComparison() {
        User user = userGenerator.one();
        LocalDateTime createdAt = LocalDateTime.now();
        user.setCreatedAt(createdAt);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new Equal<>(User_.CREATED_AT, createdAt))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(User_.CREATED_AT, createdAt.plusSeconds(1)))).isEmpty();
    }

    @Test
    void fullConstructorCombination() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME, "TestUser"))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME, "TestUser", true))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME, "TestUser", NullHandling.IGNORE))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(User_.USERNAME, "TestUser", NullHandling.USE_IS_NULL, true))).isPresent();
    }
}