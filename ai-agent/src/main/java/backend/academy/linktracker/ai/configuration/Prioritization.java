package backend.academy.linktracker.ai.configuration;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Prioritization {
    private List<String> highKeywords = List.of();
    private List<String> lowKeywords = List.of();
}
