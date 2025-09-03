package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.DatabaseTest;
import io.github.kovalev.specificationhelper.domain.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmptyTest extends DatabaseTest {

    @Test
    void returnAllEntities() {
        List<User> users = transactionalExecutor.executeWithInNewTransaction(() -> userGenerator.list(10).stream()
                .peek(user -> entityManager.persist(user))
                .toList());

        Empty<User> emptySpec = new Empty<>();

        assertThat(userRepository.findAll(emptySpec)).hasSize(users.size());
    }
}