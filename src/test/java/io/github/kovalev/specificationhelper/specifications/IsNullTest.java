package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.DatabaseTest;
import io.github.kovalev.specificationhelper.domain.entity.User;
import io.github.kovalev.specificationhelper.domain.entity.User_;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsNullTest extends DatabaseTest {

    @Test
    void whenNotDataThenOptionalIsEmpty() {
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(userGenerator.one()));

        val usernameSpec = new IsNull<User>(User_.USERNAME);
        val emailSpec = new IsNull<User>(User_.EMAIL);
        val createdAtSpec = new IsNull<User>(User_.CREATED_AT);

        assertThat(userRepository.findOne(usernameSpec)).isEmpty();
        assertThat(userRepository.findOne(emailSpec)).isEmpty();
        assertThat(userRepository.findOne(createdAtSpec)).isEmpty();
    }

    @Test
    void whenExistDataThenOptionalIsNotEmpty() {
        User user = userGenerator.one();
        user.setEmail(null);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        val emailSpec = new IsNull<User>(User_.EMAIL);
        assertThat(userRepository.findOne(emailSpec)).isPresent();
    }
}