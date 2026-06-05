package backend.academy.linktracker.scrapper;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "app.database-access-type=SQL")
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class SqlLinksRepositoryTest extends IntegrationEnvironment {}
