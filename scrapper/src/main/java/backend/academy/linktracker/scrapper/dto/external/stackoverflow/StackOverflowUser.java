package backend.academy.linktracker.scrapper.dto.external.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StackOverflowUser(String login) {}
