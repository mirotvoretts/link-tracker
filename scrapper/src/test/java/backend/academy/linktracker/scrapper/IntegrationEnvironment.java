package backend.academy.linktracker.scrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Tag("integration")
@Testcontainers
public abstract class IntegrationEnvironment {

    public static PostgreSQLContainer<?> POSTGRES;
    public static GenericContainer<?> REDIS;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("scrapper")
                .withUsername("postgres")
                .withPassword("postgres");
        POSTGRES.start();

        REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
        REDIS.start();

        try {
            runMigrations(POSTGRES);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void runMigrations(PostgreSQLContainer<?> c) throws Exception {
        try (Connection connection = DriverManager.getConnection(c.getJdbcUrl(), c.getUsername(), c.getPassword())) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            liquibase.resource.ClassLoaderResourceAccessor resourceAccessor =
                    new liquibase.resource.ClassLoaderResourceAccessor(IntegrationEnvironment.class.getClassLoader());

            Liquibase liquibase = new Liquibase("db/changelog/master.xml", resourceAccessor, database);
            liquibase.update(new Contexts(), new LabelExpression());
        }
    }

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.redis.host", REDIS::getHost);
        registry.add("spring.redis.port", () -> REDIS.getMappedPort(6379));
    }
}
