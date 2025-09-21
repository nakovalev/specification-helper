package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.DatabaseTest;
import io.github.kovalev.specificationhelper.domain.entity.User;
import io.github.kovalev.specificationhelper.domain.entity.User_;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AndTest extends DatabaseTest {

    @Test
    void emptyAndReturnsAll() {
        int count = 5;
        transactionalExecutor.executeWithInNewTransaction(() -> userGenerator.list(count).forEach(entityManager::persist));

        And<User> and = new And<>();
        assertThat(userRepository.findAll(and)).hasSize(count);
    }

    @Test
    void singleConditionWorks() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        And<User> and = new And<>(new Equal<>(User_.ID, user.getId()));
        assertThat(userRepository.findAll(and)).hasSize(1).containsExactly(user);
    }

    @Test
    void multipleConditionsWorkWhenAllMatch() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        And<User> and = new And<>(
                new Equal<>(User_.USERNAME, user.getUsername()),
                new Equal<>(User_.EMAIL, user.getEmail())
        );
        assertThat(userRepository.findAll(and)).hasSize(1).containsExactly(user);
    }

    @Test
    void noResultsWhenOneConditionFails() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        And<User> and = new And<>(
                new Equal<>(User_.USERNAME, user.getUsername()),
                new Equal<>(User_.EMAIL, "any@email.ru")
        );
        assertThat(userRepository.findAll(and)).isEmpty();
    }

    @Test
    void canAddConditionsAfterCreation() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        And<User> and = new And<>(new Equal<>(User_.USERNAME, user.getUsername()));
        and.add(new Equal<>(User_.EMAIL, user.getEmail()));

        assertThat(userRepository.findAll(and)).hasSize(1).containsExactly(user);
    }

    @Test
    void worksWithDifferentSpecificationTypes() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        And<User> and = new And<>(
                new Equal<>(User_.USERNAME, user.getUsername()),
                new GreaterThanOrEqualTo<>(User_.CREATED_AT, LocalDateTime.now().minusDays(2))
        );
        assertThat(userRepository.findAll(and)).hasSize(1).containsExactly(user);
    }

    @Test
    void collectionConstructorWorks() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        List<Specification<User>> specs = List.of(
                new Equal<>(User_.USERNAME, user.getUsername()),
                new Equal<>(User_.EMAIL, user.getEmail())
        );

        And<User> and = new And<>(specs);
        assertThat(userRepository.findAll(and)).hasSize(1).containsExactly(user);
    }
}