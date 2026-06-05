package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.external.stackoverflow.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.dto.external.stackoverflow.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.dto.external.stackoverflow.StackOverflowQuestionResponse;
import backend.academy.linktracker.scrapper.property.StackoverflowProperties;
import backend.academy.linktracker.scrapper.util.MdcUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class StackOverflowClient {
    private static final String STACKOVERFLOW_API_URL = "https://api.stackexchange.com/2.3";
    private final RestClient restClient;

    public StackOverflowClient(StackoverflowProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(STACKOVERFLOW_API_URL)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public StackOverflowQuestionResponse fetchQuestion(long questionId) {
        try {
            MdcUtil.putUrl("stackoverflow.com/questions/" + questionId);
            log.info("Fetching StackOverflow question info");

            return restClient
                    .get()
                    .uri("/questions/{ids}?site=stackoverflow&order=desc&sort=activity", questionId)
                    .retrieve()
                    .body(StackOverflowQuestionResponse.class);
        } finally {
            MdcUtil.clearUrl();
        }
    }

    public StackOverflowAnswerResponse fetchAnswer(long answerId) {
        try {
            MdcUtil.putUrl("stackoverflow.com/answer/" + answerId);
            log.info("Fetching StackOverflow answer info");

            return restClient
                    .get()
                    .uri("/answers/{ids}?site=stackoverflow&order=desc&sort=activity", answerId)
                    .retrieve()
                    .body(StackOverflowAnswerResponse.class);
        } finally {
            MdcUtil.clearUrl();
        }
    }

    public StackOverflowCommentResponse fetchComment(long commentId) {
        try {
            MdcUtil.putUrl("stackoverflow.com/comment/" + commentId);
            log.info("Fetching StackOverflow comment info");

            return restClient
                    .get()
                    .uri("/comments/{ids}?site=stackoverflow&order=desc&sort=creation", commentId)
                    .retrieve()
                    .body(StackOverflowCommentResponse.class);
        } finally {
            MdcUtil.clearUrl();
        }
    }
}
