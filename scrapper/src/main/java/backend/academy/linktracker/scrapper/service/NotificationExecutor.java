package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.BotClient;
import backend.academy.linktracker.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.util.MdcUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.notifier", name = "type", havingValue = "http", matchIfMissing = true)
@RequiredArgsConstructor
public class NotificationExecutor {

    private static final String TOPIC = "link-updates";

    private final BotClient botClient;

    private final KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;

    @TimeLimiter(name = "botClient")
    @Retry(name = "botClient")
    @CircuitBreaker(name = "botClient", fallbackMethod = "fallbackToKafkaAsync")
    public CompletableFuture<Void> sendViaHttpAsync(LinkUpdateRequest updateRequest) {
        return CompletableFuture.runAsync(() -> {
            try {
                MdcUtil.putUrl(updateRequest.url());
                MdcUtil.putLinkId(updateRequest.id());
                log.info("Sending notification via HTTP to BotClient (async)");
                botClient.sendUpdate(updateRequest);
                log.info("Notification sent successfully via HTTP (async)");
            } finally {
                MdcUtil.clearUrl();
                MdcUtil.clearLinkId();
            }
        });
    }

    public CompletableFuture<Void> fallbackToKafkaAsync(LinkUpdateRequest updateRequest, Throwable t) {
        return CompletableFuture.runAsync(() -> {
            try {
                MdcUtil.putUrl(updateRequest.url());
                MdcUtil.putLinkId(updateRequest.id());
                log.warn("Falling back to Kafka notifier (async) due to: {}", t == null ? "unknown" : t.toString());
                kafkaTemplate.send(TOPIC, String.valueOf(updateRequest.id()), updateRequest);
            } finally {
                MdcUtil.clearUrl();
                MdcUtil.clearLinkId();
            }
        });
    }
}
