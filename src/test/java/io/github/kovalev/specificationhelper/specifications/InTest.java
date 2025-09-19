package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.DatabaseTest;
import io.github.kovalev.specificationhelper.domain.entity.User;
import io.github.kovalev.specificationhelper.domain.entity.User_;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class InTest extends DatabaseTest {

    @Test
    void whenDataContainsThenNotEmptyList() {
        List<UUID> userUuids = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(10).stream()
                        .peek(user -> entityManager.persist(user))
                        .map(User::getId)
                        .toList()
                        .subList(0, 3));

        In<User, UUID> uuidSpec = new In<>(userUuids, User_.ID);
        List<User> result = userRepository.findAll(uuidSpec);

        assertThat(result).hasSize(3);
    }

    @Test
    void whenDataDoesNotContainsThenNotEmptyList() {
        transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(10).forEach(user -> entityManager.persist(user)));

        List<UUID> uuids = IntStream.range(0, 5).mapToObj(i -> UUID.randomUUID()).toList();

        In<User, UUID> uuidSpec = new In<>(uuids, User_.ID);
        List<User> result = userRepository.findAll(uuidSpec);

        assertThat(result).isEmpty();
    }

    @Test
    void whenParamsIsNotNullWhenFindAll() {
        List<UUID> userUuids = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(10).stream()
                        .peek(user -> entityManager.persist(user))
                        .map(User::getId)
                        .toList());

        In<User, UUID> emptySpec1 = new In<>(null, User_.ID);
        In<User, UUID> emptySpec2 = new In<>(List.of(), User_.ID);

        assertThat(userRepository.findAll(emptySpec1)).hasSize(userUuids.size());
        assertThat(userRepository.findAll(emptySpec2)).hasSize(userUuids.size());
    }
}