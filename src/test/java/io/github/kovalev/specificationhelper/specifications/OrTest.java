package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.DatabaseTest;
import io.github.kovalev.specificationhelper.domain.entity.User;
import io.github.kovalev.specificationhelper.domain.entity.User_;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrTest extends DatabaseTest {

    @Test
    void emptyOrReturnsEmpty() {
        Or<User> or = new Or<>();
        assertThat(userRepository.findAll(or)).isEmpty();
    }

    @Test
    void singleConditionWorks() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        Or<User> or = new Or<>(new Equal<>(User_.ID, user.getId()));
        assertThat(userRepository.findAll(or)).hasSize(1).containsExactly(user);
    }

    @Test
    void returnsResultsMatchingAnyCondition() {
        User user1 = userGenerator.one();
        User user2 = userGenerator.one();

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user1);
            entityManager.persist(user2);
        });

        Or<User> or = new Or<>(
                new Equal<>(User_.USERNAME, user1.getUsername()),
                new Equal<>(User_.USERNAME, user2.getUsername())
        );
        assertThat(userRepository.findAll(or)).hasSize(2).contains(user1, user2);
    }

    @Test
    void returnsOnlyMatchingResults() {
        User matchingUser = userGenerator.one();
        User nonMatchingUser = userGenerator.one();

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(matchingUser);
            entityManager.persist(nonMatchingUser);
        });

        Or<User> or = new Or<>(
                new Equal<>(User_.USERNAME, matchingUser.getUsername()),
                new Equal<>(User_.USERNAME, "nonexistent")
        );
        assertThat(userRepository.findAll(or)).hasSize(1).containsExactly(matchingUser);
    }

    @Test
    void worksWithDifferentSpecificationTypes() {
        User user1 = userGenerator.one();
        User user2 = userGenerator.one();
        user2.setCreatedAt(LocalDateTime.now().minusDays(1));

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user1);
            entityManager.persist(user2);
        });

        Or<User> or = new Or<>(
                new Equal<>(User_.USERNAME, user1.getUsername()),
                new LessThanOrEqualTo<>(User_.CREATED_AT, LocalDateTime.now())
        );
        assertThat(userRepository.findAll(or)).hasSize(2).contains(user1, user2);
    }

    @Test
    void collectionConstructorWorks() {
        User user1 = userGenerator.one();
        User user2 = userGenerator.one();

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user1);
            entityManager.persist(user2);
        });

        List<Specification<User>> specs = List.of(
                new Equal<>(User_.USERNAME, user1.getUsername()),
                new Equal<>(User_.USERNAME, user2.getUsername())
        );

        Or<User> or = new Or<>(specs);
        assertThat(userRepository.findAll(or)).hasSize(2).contains(user1, user2);
    }

    @Test
    void combinesWithAndCorrectly() {
        User user = userGenerator.one();
        User nonMatchingUser = userGenerator.one();

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user);
            entityManager.persist(nonMatchingUser);
        });

        Or<User> or = new Or<>(
                new Equal<>(User_.EMAIL, user.getEmail()),
                new Equal<>(User_.EMAIL, "alternative@example.com")
        );

        And<User> and = new And<>(
                new Equal<>(User_.USERNAME, user.getUsername()),
                or
        );

        assertThat(userRepository.findAll(and)).hasSize(1).containsExactly(user);
    }
}