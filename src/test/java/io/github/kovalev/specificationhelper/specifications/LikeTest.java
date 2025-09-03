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
        assertThat(userRepository.findOne(new Like<>("john", User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Like<>("doe", User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Like<>("hn_do", User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Like<>("unknown", User_.USERNAME))).isEmpty();
    }

    @Test
    void caseSensitiveSearch() {
        User user = userGenerator.one();
        user.setUsername("JohnDoe");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Case-sensitive поиск (по умолчанию)
        assertThat(userRepository.findOne(new Like<>("john", User_.USERNAME))).isEmpty();
        assertThat(userRepository.findOne(new Like<>("John", User_.USERNAME))).isPresent();
    }

    @Test
    void caseInsensitiveSearch() {
        User user = userGenerator.one();
        user.setUsername("JohnDoe");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Case-insensitive поиск
        assertThat(userRepository.findOne(new Like<>("john", true, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Like<>("DOE", true, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Like<>("hNd", true, User_.USERNAME))).isPresent();
    }

    @Test
    void LikeMatchModeVariations() {
        User user = userGenerator.one();
        user.setUsername("searchterm");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // START_ONLY - ищем в конце строки
        assertThat(userRepository.findOne(new Like<>("term", LikeMatchMode.START_ONLY, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Like<>("search", LikeMatchMode.START_ONLY, User_.USERNAME))).isEmpty();

        // END_ONLY - ищем в начале строки
        assertThat(userRepository.findOne(new Like<>("search", LikeMatchMode.END_ONLY, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Like<>("term", LikeMatchMode.END_ONLY, User_.USERNAME))).isEmpty();

        // NONE - точное совпадение (эквивалент equals)
        assertThat(userRepository.findOne(new Like<>("searchterm", LikeMatchMode.NONE, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Like<>("search", LikeMatchMode.NONE, User_.USERNAME))).isEmpty();
    }

    @Test
    void standardLikeBehavior() {
        User user = userGenerator.one();
        user.setUsername("john_doe");
        user.setEmail("100%_match@example.com");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // _ работает как single-character wildcard
        assertThat(userRepository.findOne(new Like<>("j%_d%", User_.USERNAME))).isPresent();

        // Поиск реальных спецсимволов с экранированием
        assertThat(userRepository.findOne(new Like<>("100\\%\\_match", User_.EMAIL))).isPresent();

        // Смешанный случай
        assertThat(userRepository.findOne(new Like<>("10%\\_ma%", User_.EMAIL))).isPresent();
    }

    @Test
    void edgeCases() {
        User user = userGenerator.one();
        user.setUsername("\\_escape_test");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Поиск escape-символа
        assertThat(userRepository.findOne(new Like<>("\\\\_esc%", User_.USERNAME))).isPresent();
    }

    @Test
    void nullValueHandling() {
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(userGenerator.one()));

        // При null значении - пустая спецификация
        assertThat(userRepository.findOne(new Like<>(null, User_.USERNAME))).isPresent();
    }

    @Test
    void fullConstructorCombination() {
        User user = userGenerator.one();
        user.setUsername("SearchTerm");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Проверка всех вариантов конструктора
        assertThat(userRepository.findOne(new Like<>("term", User_.USERNAME))).isEmpty();
        assertThat(userRepository.findOne(new Like<>("term", true, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Like<>("term", LikeMatchMode.END_ONLY, User_.USERNAME))).isEmpty();
        assertThat(userRepository.findOne(new Like<>("term", LikeMatchMode.START_ONLY, true, User_.USERNAME))).isPresent();
    }

    @Test
    void exactMatchWithNoWildcards() {
        User user = userGenerator.one();
        user.setUsername("exactmatch");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Точное совпадение без wildcards
        assertThat(userRepository.findOne(new Like<>("exactmatch", LikeMatchMode.NONE, User_.USERNAME))).isPresent();
        assertThat(userRepository.findOne(new Like<>("exact", LikeMatchMode.NONE, User_.USERNAME))).isEmpty();
    }
}