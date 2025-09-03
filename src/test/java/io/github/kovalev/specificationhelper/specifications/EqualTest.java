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

        assertThat(userRepository.findOne(new Equal<>(user.getId(), User_.ID))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(user.getUsername(), User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(user.getEmail(), User_.EMAIL))).isPresent();

        assertThat(userRepository.findOne(new Equal<>("nonexistent", User_.USERNAME))).isEmpty();
    }

    @Test
    void caseSensitiveComparison() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new Equal<>("TestUser", User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Equal<>("testuser", User_.USERNAME))).isEmpty();
        assertThat(userRepository.findOne(new Equal<>("TESTUSER", User_.USERNAME))).isEmpty();
    }

    @Test
    void caseInsensitiveComparison() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new Equal<>("TestUser", true, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Equal<>("testuser", true, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Equal<>("TESTUSER", true, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Equal<>("different", true, User_.USERNAME))).isEmpty();
    }

    @Test
    void ignoreNullValue() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));
        assertThat(userRepository.findOne(new Equal<>(null, User_.ID))).isPresent();
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

        val emailIsNull = new Equal<User>(null, NullHandling.USE_IS_NULL, User_.EMAIL);
        assertThat(userRepository.findAll(emailIsNull)).hasSize(1).allMatch(u -> u.getEmail() == null);

        val usernameEqual = new Equal<User>("test", NullHandling.USE_IS_NULL, User_.USERNAME);
        assertThat(userRepository.findOne(usernameEqual)).isEmpty();
    }

    @Test
    void nonStringValueComparison() {
        User user = userGenerator.one();
        LocalDateTime createdAt = LocalDateTime.now();
        user.setCreatedAt(createdAt);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new Equal<>(createdAt, User_.CREATED_AT))).isPresent();
        assertThat(userRepository.findOne(new Equal<>(createdAt.plusSeconds(1), User_.CREATED_AT))).isEmpty();
    }

    @Test
    void fullConstructorCombination() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new Equal<>("TestUser", User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Equal<>("TestUser", true, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Equal<>("TestUser", NullHandling.IGNORE, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Equal<>("TestUser", NullHandling.USE_IS_NULL, true, User_.USERNAME))).isPresent();
    }
}