package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.DatabaseTest;
import io.github.kovalev.specificationhelper.domain.entity.User;
import io.github.kovalev.specificationhelper.domain.entity.User_;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsNotNullTest extends DatabaseTest {

    @Test
    void whenNullDataThenOptionalIsEmpty() {
        User user = userGenerator.one();
        user.setEmail(null);

        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        val emailSpec = new IsNotNull<User>(User_.EMAIL);
        assertThat(userRepository.findOne(emailSpec)).isEmpty();

        val notIsNullSpec = new Not<User>(new IsNull<>(User_.EMAIL));
        assertThat(userRepository.findOne(emailSpec))
                .isEqualTo(userRepository.findOne(notIsNullSpec));
    }

    @Test
    void whenNotNullDataThenOptionalIsNotEmpty() {
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(userGenerator.one()));

        val emailSpec = new IsNotNull<User>(User_.EMAIL);
        assertThat(userRepository.findOne(emailSpec)).isPresent();

        val notIsNullSpec = new Not<User>(new IsNull<>(User_.EMAIL));
        assertThat(userRepository.findOne(emailSpec))
                .isEqualTo(userRepository.findOne(notIsNullSpec));
    }
}