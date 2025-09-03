package io.github.kovalev.specificationhelper.configuration;


import io.github.kovalev.specificationhelper.testutils.TransactionalExecutor;
import io.github.kovalev.specificationhelper.testutils.UserGenerator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Random;

@TestConfiguration
public class TestConfig {

    @Bean
    TransactionalExecutor transactionalExecutor() {
        return new TransactionalExecutor();
    }

    @Bean
    Random random() {
        return new Random();
    }

    @Bean
    UserGenerator userGenerator(Random random) {
        return new UserGenerator(random);
    }
}
