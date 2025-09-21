package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.DatabaseTest;
import io.github.kovalev.specificationhelper.domain.entity.User;
import io.github.kovalev.specificationhelper.domain.entity.User_;
import io.github.kovalev.specificationhelper.enums.LikeMatchMode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeTest extends DatabaseTest {

    @Test
    void basicLikeSearch() {
        User user = userGenerator.one();
        user.setUsername("john_doe");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Поиск с wildcards с обеих сторон (по умолчанию)
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "john"))).isPresent();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "doe"))).isPresent();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "hn_do"))).isPresent();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "unknown"))).isEmpty();
    }

    @Test
    void caseSensitiveSearch() {
        User user = userGenerator.one();
        user.setUsername("JohnDoe");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Case-sensitive поиск (по умолчанию)
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "john"))).isEmpty();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "John"))).isPresent();
    }

    @Test
    void caseInsensitiveSearch() {
        User user = userGenerator.one();
        user.setUsername("JohnDoe");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Case-insensitive поиск
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "john", true))).isPresent();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "DOE", true))).isPresent();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "hNd", true))).isPresent();
    }

    @Test
    void LikeMatchModeVariations() {
        User user = userGenerator.one();
        user.setUsername("searchterm");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // START_ONLY - ищем в конце строки
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "term", LikeMatchMode.START_ONLY))).isPresent();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "search", LikeMatchMode.START_ONLY))).isEmpty();

        // END_ONLY - ищем в начале строки
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "search", LikeMatchMode.END_ONLY))).isPresent();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "term", LikeMatchMode.END_ONLY))).isEmpty();

        // NONE - точное совпадение (эквивалент equals)
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "searchterm", LikeMatchMode.NONE))).isPresent();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "search", LikeMatchMode.NONE))).isEmpty();
    }

    @Test
    void standardLikeBehavior() {
        User user = userGenerator.one();
        user.setUsername("john_doe");
        user.setEmail("100%_match@example.com");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // _ работает как single-character wildcard
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "j%_d%"))).isPresent();

        // Поиск реальных спецсимволов с экранированием
        assertThat(userRepository.findOne(new Like<>(User_.EMAIL, "100\\%\\_match"))).isPresent();

        // Смешанный случай
        assertThat(userRepository.findOne(new Like<>(User_.EMAIL, "10%\\_ma%"))).isPresent();
    }

    @Test
    void edgeCases() {
        User user = userGenerator.one();
        user.setUsername("\\_escape_test");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Поиск escape-символа
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "\\\\_esc%"))).isPresent();
    }

    @Test
    void nullValueHandling() {
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(userGenerator.one()));

        // При null значении - пустая спецификация
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, null))).isPresent();
    }

    @Test
    void fullConstructorCombination() {
        User user = userGenerator.one();
        user.setUsername("SearchTerm");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Проверка всех вариантов конструктора
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "term"))).isEmpty();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "term", true))).isPresent();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "term", LikeMatchMode.END_ONLY))).isEmpty();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "term", LikeMatchMode.START_ONLY, true))).isPresent();
    }

    @Test
    void exactMatchWithNoWildcards() {
        User user = userGenerator.one();
        user.setUsername("exactmatch");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Точное совпадение без wildcards
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "exactmatch", LikeMatchMode.NONE))).isPresent();
        assertThat(userRepository.findOne(new Like<>(User_.USERNAME, "exact", LikeMatchMode.NONE))).isEmpty();
    }
}