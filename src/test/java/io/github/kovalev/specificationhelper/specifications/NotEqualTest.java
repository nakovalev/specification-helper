package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.DatabaseTest;
import io.github.kovalev.specificationhelper.domain.entity.User;
import io.github.kovalev.specificationhelper.domain.entity.User_;
import io.github.kovalev.specificationhelper.enums.NullHandling;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotEqualTest extends DatabaseTest {

    @Test
    void notEqualWithNonNullValues() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new NotEqual<>("nonexistent", User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>(UUID.randomUUID(), User_.ID))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>(user.getEmail() + "x", User_.EMAIL))).isPresent();

        assertThat(userRepository.findOne(new NotEqual<>(user.getUsername(), User_.USERNAME))).isEmpty();
    }

    @Test
    void caseSensitiveComparison() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new NotEqual<>("testuser", User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>("TESTUSER", User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>("TestUser", User_.USERNAME))).isEmpty();
    }

    @Test
    void caseInsensitiveComparison() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new NotEqual<>("testuser", true, User_.USERNAME))).isEmpty();
        assertThat(userRepository.findOne(new NotEqual<>("TESTUSER", true, User_.USERNAME))).isEmpty();
        assertThat(userRepository.findOne(new NotEqual<>("TestUser", true, User_.USERNAME))).isEmpty();
        assertThat(userRepository.findOne(new NotEqual<>("different", true, User_.USERNAME))).isPresent();
    }

    @Test
    void ignoreNullValue() {
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(userGenerator.one()));

        assertThat(userRepository.findOne(new NotEqual<>(null, User_.ID))).isPresent();
    }

    @Test
    void addNullAsIsNotNull() {
        User userWithNullEmail = userGenerator.one();
        userWithNullEmail.setEmail(null);

        User userWithEmail = userGenerator.one();
        userWithEmail.setEmail("test@example.com");

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(userWithNullEmail);
            entityManager.persist(userWithEmail);
        });

        val emailIsNotNull = new NotEqual<User>(null, NullHandling.USE_IS_NULL, User_.EMAIL);
        assertThat(userRepository.findAll(emailIsNotNull))
                .hasSize(1)
                .allMatch(u -> u.getEmail() != null);
    }


    @Test
    void nonStringValueNotEqual() {
        User user = userGenerator.one();
        LocalDateTime createdAt = LocalDateTime.now();
        user.setCreatedAt(createdAt);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));


        assertThat(userRepository.findOne(new NotEqual<>(createdAt.plusSeconds(1), User_.CREATED_AT))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>(createdAt, User_.CREATED_AT))).isEmpty();
    }

    @Test
    void fullConstructorCombination() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new NotEqual<>("testuser", User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>("testuser", true, User_.USERNAME))).isEmpty();
        assertThat(userRepository.findOne(new NotEqual<>("testuser", NullHandling.IGNORE, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>("testuser", NullHandling.USE_IS_NULL, true, User_.USERNAME))).isEmpty();
    }
}