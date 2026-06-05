package backend.academy.linktracker.scrapper.property;

import backend.academy.linktracker.scrapper.configuration.AccessType;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
        @NotNull AccessType databaseAccessType,
        @NotNull BotProperties bot,
        @NotNull GithubProperties github,
        @NotNull StackoverflowProperties stackoverflow,
        @NotNull Scheduler scheduler,
        @NotNull Notifier notifier,
        @NotNull Cache cache) {
    public record Scheduler(
            @NotNull @DurationUnit(ChronoUnit.MILLIS) Duration interval, int packageSize, int threads) {}

    public record Notifier(@NotNull String type) {}

    public record Cache(@NotNull UserLinks userLinks) {}

    public record UserLinks(
            @NotNull @DurationUnit(ChronoUnit.MILLIS) Duration timeToLive) {}
}
