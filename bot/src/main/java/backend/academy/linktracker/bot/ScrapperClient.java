package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.dto.request.ListLinksRequest;
import backend.academy.linktracker.bot.dto.result.AddLinkResult;
import backend.academy.linktracker.bot.dto.result.DeleteLinkResult;
import backend.academy.linktracker.bot.property.ScrapperProperties;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class ScrapperClient {
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";
    private static final String LINKS_URI = "/links";

    private final RestClient restClient;

    public ScrapperClient(ScrapperProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).build();
    }

    public void registerChat(long chatId) {
        log.atInfo().addKeyValue("chatId", chatId).log("Registering chat in scrapper");
        restClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
    }

    public ListLinksRequest getLinks(long chatId) {
        log.atInfo().addKeyValue("chatId", chatId).log("Getting links from scrapper");
        try {
            return restClient
                    .get()
                    .uri(LINKS_URI)
                    .headers(headers -> headers.set(TG_CHAT_ID_HEADER, String.valueOf(chatId)))
                    .retrieve()
                    .body(ListLinksRequest.class);
        } catch (HttpClientErrorException.NotFound _) {
            return null;
        }
    }

    public AddLinkResult addLink(long chatId, String link, List<String> tags) {
        log.atInfo().addKeyValue("chatId", chatId).addKeyValue("link", link).log("Adding link in scrapper");
        try {
            restClient
                    .post()
                    .uri(LINKS_URI)
                    .headers(headers -> headers.set(TG_CHAT_ID_HEADER, String.valueOf(chatId)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("link", link, "tags", tags != null ? tags : List.of()))
                    .retrieve()
                    .toBodilessEntity();
            return AddLinkResult.OK;
        } catch (HttpClientErrorException.Conflict _) {
            return AddLinkResult.ALREADY_EXISTS;
        } catch (HttpClientErrorException.NotFound _) {
            return AddLinkResult.NOT_FOUND;
        }
    }

    public DeleteLinkResult removeLink(long chatId, String link) {
        log.atInfo().addKeyValue("chatId", chatId).addKeyValue("link", link).log("Removing link in scrapper");
        try {
            restClient
                    .method(HttpMethod.DELETE)
                    .uri(LINKS_URI)
                    .headers(headers -> headers.set(TG_CHAT_ID_HEADER, String.valueOf(chatId)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("link", link))
                    .retrieve()
                    .toBodilessEntity();
            return DeleteLinkResult.OK;
        } catch (HttpClientErrorException.NotFound _) {
            return DeleteLinkResult.NOT_FOUND;
        }
    }
}
