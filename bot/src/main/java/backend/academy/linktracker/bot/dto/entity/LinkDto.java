package backend.academy.linktracker.bot.dto.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LinkDto {
    private long id;
    private String url;
    private List<String> tags;
}
