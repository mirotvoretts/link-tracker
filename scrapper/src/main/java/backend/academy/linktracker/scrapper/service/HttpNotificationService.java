package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.request.LinkUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.notifier", name = "type", havingValue = "http", matchIfMissing = true)
@RequiredArgsConstructor
public class HttpNotificationService implements NotificationService {

    private final NotificationExecutor notificationExecutor;

    @Override
    public void sendUpdate(LinkUpdateRequest updateRequest) {
        try {
            notificationExecutor.sendViaHttpAsync(updateRequest).join();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    @Override
    public String getName() {
        return "HTTP";
    }
}
