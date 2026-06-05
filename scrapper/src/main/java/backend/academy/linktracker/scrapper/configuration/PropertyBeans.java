package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.property.ApplicationConfig;
import backend.academy.linktracker.scrapper.property.BotProperties;
import backend.academy.linktracker.scrapper.property.GithubProperties;
import backend.academy.linktracker.scrapper.property.StackoverflowProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertyBeans {

    @Bean
    public BotProperties botProperties(ApplicationConfig config) {
        return config.bot();
    }

    @Bean
    public GithubProperties githubProperties(ApplicationConfig config) {
        return config.github();
    }

    @Bean
    public StackoverflowProperties stackoverflowProperties(ApplicationConfig config) {
        return config.stackoverflow();
    }
}
