package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.IntegrationEnvironment;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LinksCacheInvalidationTest extends IntegrationEnvironment {}
