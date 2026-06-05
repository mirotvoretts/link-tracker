package backend.academy.linktracker.scrapper.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:29092", "advertised.listeners=PLAINTEXT://localhost:29092"
        })
@TestPropertySource(properties = {"spring.kafka.bootstrap-servers=localhost:29092", "app.notifier.type=kafka"})
class KafkaIntegrationTest {}
