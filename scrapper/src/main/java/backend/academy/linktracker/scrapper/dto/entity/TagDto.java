package backend.academy.linktracker.scrapper.dto.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TagDto implements Serializable {
    private static final long serialVersionUID = 1L;

    String name;
}
