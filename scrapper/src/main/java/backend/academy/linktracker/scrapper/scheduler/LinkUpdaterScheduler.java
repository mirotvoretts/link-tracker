package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.LinkProcessingReport;
import backend.academy.linktracker.scrapper.dto.LinkWithChatId;
import backend.academy.linktracker.scrapper.dto.entity.LinkDto;
import backend.academy.linktracker.scrapper.dto.external.LinkChangeDetails;
import backend.academy.linktracker.scrapper.dto.external.github.GitHubIssue;
import backend.academy.linktracker.scrapper.dto.external.github.GitHubPullRequest;
import backend.academy.linktracker.scrapper.dto.external.github.GitHubRepoResponse;
import backend.academy.linktracker.scrapper.dto.external.stackoverflow.StackOverflowQuestionResponse;
import backend.academy.linktracker.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.property.ApplicationConfig;
import backend.academy.linktracker.scrapper.service.LinksService;
import backend.academy.linktracker.scrapper.service.NotificationService;
import backend.academy.linktracker.scrapper.util.MdcUtil;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkUpdaterScheduler {

    private static final Pattern GITHUB_PATTERN = Pattern.compile("https?://github\\.com/([^/]+)/([^/]+)");
    private static final Pattern STACKOVERFLOW_PATTERN =
            Pattern.compile("https?://stackoverflow\\.com/questions/(\\d+)");

    private final LinksService linksService;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final NotificationService notificationService;
    private final ApplicationConfig applicationConfig;

    @Scheduled(fixedDelayString = "${app.scheduler.interval:60000}")
    public void update() {
        log.info("Starting scheduled link update check");

        Map<Long, Set<LinkDto>> allLinks = linksService.getAllLinksGroupedByChat();

        int packageSize = applicationConfig.scheduler().packageSize();
        int threadCount = applicationConfig.scheduler().threads();

        log.info("Using batch processing configuration: packageSize={}, threads={}", packageSize, threadCount);

        processBatchesWithThreads(allLinks, packageSize, threadCount);
    }

    private void processBatchesWithThreads(Map<Long, Set<LinkDto>> allLinks, int packageSize, int threadCount) {
        List<LinkWithChatId> allLinksList = new ArrayList<>();
        for (Map.Entry<Long, Set<LinkDto>> entry : allLinks.entrySet()) {
            long chatId = entry.getKey();
            for (LinkDto link : entry.getValue()) {
                allLinksList.add(new LinkWithChatId(chatId, link));
            }
        }

        log.info("Processing links in batches: totalLinks={}, packageSize={}", allLinksList.size(), packageSize);

        int totalBatches = (int) Math.ceil((double) allLinksList.size() / packageSize);

        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            try {
                MdcUtil.putBatchNumber(batchNum + 1);

                int startIndex = batchNum * packageSize;
                int endIndex = Math.min(startIndex + packageSize, allLinksList.size());
                List<LinkWithChatId> batch = allLinksList.subList(startIndex, endIndex);

                log.info(
                        "Processing batch: batchNumber={}, batchSize={}, totalBatches={}",
                        batchNum + 1,
                        batch.size(),
                        totalBatches);

                LinkProcessingReport report = processBatchMultiThreaded(batch, threadCount);
                logBatchResult(batchNum + 1, report);
            } finally {
                MdcUtil.clearBatchNumber();
            }
        }

        log.info("Batch processing completed: totalLinksProcessed={}", allLinksList.size());
    }

    private LinkProcessingReport processBatchMultiThreaded(List<LinkWithChatId> batch, int threadCount) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<LinkProcessingReport.LinkProcessingError> errors = new ArrayList<>();
        int successCount = batch.size();

        try {
            int subBatchSize = (int) Math.ceil((double) batch.size() / threadCount);
            List<java.util.concurrent.Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < batch.size(); i += subBatchSize) {
                int endIndex = Math.min(i + subBatchSize, batch.size());
                List<LinkWithChatId> subBatch = batch.subList(i, endIndex);

                java.util.concurrent.Future<?> future = executorService.submit(() -> {
                    MdcUtil.putThreadName(Thread.currentThread().getName());
                    try {
                        for (LinkWithChatId linkWithChat : subBatch) {
                            checkLink(linkWithChat.chatId(), linkWithChat.link());
                        }
                    } finally {
                        MdcUtil.clearThreadName();
                    }
                });

                futures.add(future);
            }

            for (java.util.concurrent.Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.error("Error in processing thread", e);
                    successCount--;
                }
            }
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return new LinkProcessingReport(batch.size(), successCount, errors.size(), errors);
    }

    private void logBatchResult(int batchNumber, LinkProcessingReport report) {
        log.info(
                "Batch processing result: batch={}, total={}, successful={}, failed={}",
                batchNumber,
                report.totalLinks(),
                report.successfulLinks(),
                report.failedLinks());

        if (!report.errors().isEmpty()) {
            log.warn(
                    "Links failed to process in batch: errorCount={}",
                    report.errors().size());
            for (LinkProcessingReport.LinkProcessingError error : report.errors()) {
                log.warn("Failed to process link: linkId={}, error={}", error.linkId(), error.errorMessage());
            }
        }
    }

    private void checkLink(long chatId, LinkDto link) {
        try {
            MdcUtil.putChatId(chatId);
            MdcUtil.putLinkId(link.getId());
            MdcUtil.putUrl(link.getUrl());

            log.info("Starting link check: url={}", link.getUrl());

            Matcher githubMatcher = GITHUB_PATTERN.matcher(link.getUrl());
            if (githubMatcher.find()) {
                log.info("URL matches GitHub pattern");
                if (checkGitHubLink(chatId, link, githubMatcher)) {
                    log.info("GitHub check completed successfully");
                    return;
                }
            } else {
                log.info("URL does not match GitHub pattern, trying fallback");
            }

            checkLinkWithFallback(chatId, link);
            log.info("Link check completed");

        } catch (Exception e) {
            log.error("Failed to check link for updates", e);
        } finally {
            MdcUtil.clear();
        }
    }

    private boolean checkGitHubLink(long chatId, LinkDto link, Matcher githubMatcher) {
        String owner = githubMatcher.group(1);
        String repo = githubMatcher.group(2);

        MdcUtil.putOwner(owner);
        MdcUtil.putRepo(repo);

        log.info("Checking GitHub repo for updates: owner={}, repo={}", owner, repo);

        try {
            List<GitHubPullRequest> pullRequests = gitHubClient.fetchPullRequests(owner, repo);
            log.info("GitHub pull requests fetched: prCount={}", pullRequests.size());

            if (!pullRequests.isEmpty()) {
                GitHubPullRequest latestPR = pullRequests.getFirst();
                MdcUtil.putPrNumber(latestPR.number());

                log.info(
                        "Latest PR info: prNumber={}, updatedAt={}, linkLastCheckedAt={}",
                        latestPR.number(),
                        latestPR.updatedAt(),
                        link.getLastCheckedAt());

                if (link.getLastCheckedAt() == null || latestPR.updatedAt().isAfter(link.getLastCheckedAt())) {
                    log.info("New PR detected: prNumber={}, title={}", latestPR.number(), latestPR.title());

                    sendUpdateNotification(
                            chatId,
                            link,
                            "NEW_PR",
                            latestPR.title(),
                            latestPR.user().login(),
                            latestPR.createdAt(),
                            LinkChangeDetails.truncate(latestPR.body()),
                            latestPR.number());

                    link.setLastCheckedAt(OffsetDateTime.now());
                    linksService.updateLink(link);
                    log.info("Link updated in database");
                    return true;
                } else {
                    log.info("PR is not newer than last check");
                }
            }

            List<GitHubIssue> issues = gitHubClient.fetchIssues(owner, repo);
            log.info("GitHub issues fetched: issueCount={}", issues.size());

            if (!issues.isEmpty()) {
                GitHubIssue latestIssue = issues.getFirst();
                MdcUtil.putIssueNumber(latestIssue.number());

                log.info(
                        "Latest issue info: issueNumber={}, updatedAt={}, linkLastCheckedAt={}",
                        latestIssue.number(),
                        latestIssue.updatedAt(),
                        link.getLastCheckedAt());

                if (link.getLastCheckedAt() == null || latestIssue.updatedAt().isAfter(link.getLastCheckedAt())) {
                    log.info("New issue detected: issueNumber={}, title={}", latestIssue.number(), latestIssue.title());

                    sendUpdateNotification(
                            chatId,
                            link,
                            "NEW_ISSUE",
                            latestIssue.title(),
                            latestIssue.user().login(),
                            latestIssue.createdAt(),
                            LinkChangeDetails.truncate(latestIssue.body()),
                            latestIssue.number());

                    link.setLastCheckedAt(OffsetDateTime.now());
                    linksService.updateLink(link);
                    log.info("Link updated in database");
                    return true;
                } else {
                    log.info("Issue is not newer than last check");
                }
            }
        } catch (Exception e) {
            log.error("Error checking GitHub link", e);
        }

        return false;
    }

    private void checkLinkWithFallback(long chatId, LinkDto link) {
        OffsetDateTime lastUpdated = getLastUpdated(link.getUrl());
        if (lastUpdated != null && (link.getLastCheckedAt() == null || lastUpdated.isAfter(link.getLastCheckedAt()))) {

            log.info("Update detected for link");

            notificationService.sendUpdate(new LinkUpdateRequest(
                    link.getId(),
                    link.getUrl(),
                    "Обнаружено обновление по ссылке: " + link.getUrl(),
                    List.of(chatId),
                    null));

            link.setLastCheckedAt(OffsetDateTime.now());
            linksService.updateLink(link);
            log.info("Link updated in database");
        }
    }

    private void sendUpdateNotification(
            long chatId,
            LinkDto link,
            String changeType,
            String title,
            String username,
            OffsetDateTime createdAt,
            String preview,
            int number) {

        try {
            MdcUtil.putChangeType(changeType);

            log.info("Update detected: changeType={}, number={}", changeType, number);

            String displayTitle = "#" + number + " " + title;
            LinkChangeDetails changeDetails =
                    new LinkChangeDetails(changeType, displayTitle, username, createdAt, preview);

            LinkUpdateRequest updateRequest = new LinkUpdateRequest(
                    link.getId(),
                    link.getUrl(),
                    "Новый " + changeType.replace("_", " ").toLowerCase() + ": " + title,
                    List.of(chatId),
                    changeDetails);

            log.info("Sending notification");

            try {
                notificationService.sendUpdate(updateRequest);
                log.info("Notification sent successfully");
            } catch (Exception e) {
                log.error("Failed to send notification", e);
            }
        } finally {
            MdcUtil.clearChangeType();
        }
    }

    private OffsetDateTime getLastUpdated(String url) {
        Matcher githubMatcher = GITHUB_PATTERN.matcher(url);
        if (githubMatcher.find()) {
            String owner = githubMatcher.group(1);
            String repo = githubMatcher.group(2);
            GitHubRepoResponse response = gitHubClient.fetchRepository(owner, repo);
            return response.pushedAt() != null ? response.pushedAt() : response.updatedAt();
        }

        Matcher soMatcher = STACKOVERFLOW_PATTERN.matcher(url);
        if (soMatcher.find()) {
            long questionId = Long.parseLong(soMatcher.group(1));
            StackOverflowQuestionResponse response = stackOverflowClient.fetchQuestion(questionId);
            if (response != null && !response.items().isEmpty()) {
                long epoch = response.items().getFirst().lastActivityDate();
                return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
            }
        }

        log.warn("Unsupported link type for update checking");
        return null;
    }
}
