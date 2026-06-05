package backend.academy.linktracker.bot.dto.request;

import backend.academy.linktracker.bot.dto.entity.LinkDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListLinksRequest {
    private List<LinkDto> links;
    private int size;
}
