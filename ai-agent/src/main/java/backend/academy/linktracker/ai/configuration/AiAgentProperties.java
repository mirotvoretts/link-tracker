package backend.academy.linktracker.ai.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Setter
@Getter
@ConfigurationProperties(prefix = "ai-agent")
public class AiAgentProperties {
    @NestedConfigurationProperty
    private Filtering filtering = new Filtering();

    @NestedConfigurationProperty
    private Summarization summarization = new Summarization();

    @NestedConfigurationProperty
    private Prioritization prioritization = new Prioritization();

    @NestedConfigurationProperty
    private Grouping grouping = new Grouping();
}
