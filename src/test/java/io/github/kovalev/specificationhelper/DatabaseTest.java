package io.github.kovalev.specificationhelper;


import io.github.kovalev.specificationhelper.configuration.TestConfig;
import io.github.kovalev.specificationhelper.domain.jpa.ComparableRepository;
import io.github.kovalev.specificationhelper.domain.jpa.UserRepository;
import io.github.kovalev.specificationhelper.testutils.TransactionalExecutor;
import io.github.kovalev.specificationhelper.testutils.UserGenerator;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({TestApplication.class, TestConfig.class})
@TestPropertySource("classpath:application.properties")
public abstract class DatabaseTest {

    private static final String[] tableNames = {
            "users",
            "comments",
            "posts",
            "comparable_entity",
            "temporal_entity"
    };
    @ServiceConnection
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2");
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected EntityManager entityManager;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected ComparableRepository comparableRepository;
    @Autowired
    protected UserGenerator userGenerator;
    @Autowired
    protected TransactionalExecutor transactionalExecutor;

    @BeforeEach
    public void clearDb() {
        log.info("Starting database cleanup...");
        transactionalExecutor.executeWithInNewTransaction(
                () -> {
                    jdbcTemplate.execute("SET session_replication_role = 'replica'");
                    jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", tableNames) + " CASCADE");
                    jdbcTemplate.execute("SET session_replication_role = 'origin'");
                }
        );
        log.info("Database cleanup completed successfully!");
    }
}
