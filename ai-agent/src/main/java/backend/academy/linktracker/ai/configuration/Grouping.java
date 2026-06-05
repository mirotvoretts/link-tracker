package backend.academy.linktracker.ai.configuration;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Grouping {
    private long windowMs = 30000;
}
