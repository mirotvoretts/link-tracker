package backend.academy.linktracker.ai.configuration;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Filtering {
    private List<String> stopWords = List.of();
    private List<String> excludedAuthors = List.of();
    private int minLength = 0;
}
