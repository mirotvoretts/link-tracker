package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.dto.external.LinkChangeDetails;
import backend.academy.linktracker.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.service.KafkaNotificationService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class KafkaNotificationServiceTest {

    @Mock
    private KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;

    private KafkaNotificationService kafkaNotificationService;

    @BeforeEach
    void setUp() {
        kafkaNotificationService = new KafkaNotificationService(kafkaTemplate);
    }

    @Test
    void testServiceNameIsKafka() {
        String serviceName = kafkaNotificationService.getName();
        assertEquals("Kafka", serviceName);
    }

    @Test
    void testSendUpdateDoesNotThrow() {
        when(kafkaTemplate.send(anyString(), anyString(), any(LinkUpdateRequest.class)))
                .thenReturn(new CompletableFuture<>());

        LinkChangeDetails changeDetails = new LinkChangeDetails(
                "NEW_ISSUE", "Test Issue", "testuser", OffsetDateTime.now(), "This is a test issue preview");

        LinkUpdateRequest updateRequest = new LinkUpdateRequest(
                1L, "https://github.com/example/repo", "Test link", List.of(123456789L), changeDetails);

        kafkaNotificationService.sendUpdate(updateRequest);

        verify(kafkaTemplate).send(anyString(), anyString(), any(LinkUpdateRequest.class));
    }

    @Test
    void testServiceIsNotNull() {
        assertNotNull(kafkaNotificationService);
    }
}
