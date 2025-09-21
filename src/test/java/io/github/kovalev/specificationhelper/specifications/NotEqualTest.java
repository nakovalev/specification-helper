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

        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, "nonexistent"))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>(User_.ID, UUID.randomUUID()))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>(User_.EMAIL, user.getEmail() + "x"))).isPresent();

        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, user.getUsername()))).isEmpty();
    }

    @Test
    void caseSensitiveComparison() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, "testuser"))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, "TESTUSER"))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, "TestUser"))).isEmpty();
    }

    @Test
    void caseInsensitiveComparison() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, "testuser", true))).isEmpty();
        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, "TESTUSER", true))).isEmpty();
        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, "TestUser", true))).isEmpty();
        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, "different", true))).isPresent();
    }

    @Test
    void ignoreNullValue() {
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(userGenerator.one()));

        assertThat(userRepository.findOne(new NotEqual<>(User_.ID, null))).isPresent();
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

        val emailIsNotNull = new NotEqual<User>(User_.EMAIL, null, NullHandling.USE_IS_NULL);
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


        assertThat(userRepository.findOne(new NotEqual<>(User_.CREATED_AT, createdAt.plusSeconds(1)))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>(User_.CREATED_AT, createdAt))).isEmpty();
    }

    @Test
    void fullConstructorCombination() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, "testuser"))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, "testuser", true))).isEmpty();
        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, "testuser", NullHandling.IGNORE))).isPresent();
        assertThat(userRepository.findOne(new NotEqual<>(User_.USERNAME, "testuser", NullHandling.USE_IS_NULL, true))).isEmpty();
    }
}