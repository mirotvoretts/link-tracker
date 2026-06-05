package backend.academy.linktracker.scrapper.dto.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(of = "url")
public class LinkDto implements Serializable {
    private static final long serialVersionUID = 1L;

    long id;
    String url;
    List<TagDto> tags;
    OffsetDateTime lastCheckedAt;
}
