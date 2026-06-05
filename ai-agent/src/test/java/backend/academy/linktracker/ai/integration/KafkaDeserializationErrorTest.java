package backend.academy.linktracker.ai.integration;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;

@Tag("integration")
@SpringBootTest
@TestPropertySource(properties = {"ai-agent.grouping.window-ms=200"})
class KafkaDeserializationErrorTest extends KafkaIntegrationTestBase {

    @Autowired
    private KafkaTemplate<String, String> stringTemplate;

    @Test
    void shouldNotCrashOnInvalidJsonMessage() {
        String invalidJson = "{invalid json}";

        assertThatNoException().isThrownBy(() -> {
            stringTemplate.send("link.raw-updates", "1", invalidJson);
        });

        await().timeout(3, TimeUnit.SECONDS).untilAsserted(() -> {});
    }

    @Test
    void shouldNotCrashOnBinaryMessage() {
        byte[] binaryData = new byte[] {0x00, 0x01, 0x02, 0x03};

        assertThatNoException().isThrownBy(() -> {
            stringTemplate.send("link.raw-updates", "2", new String(binaryData));
        });

        await().timeout(3, TimeUnit.SECONDS).untilAsserted(() -> {});
    }
}
