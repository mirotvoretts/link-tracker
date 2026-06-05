package backend.academy.linktracker.scrapper.domain.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chats")
@Getter
@Setter
public class ChatEntity {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LinkEntity> links = new HashSet<>();

    public void addLink(LinkEntity link) {
        links.add(link);
        link.setChat(this);
    }

    public void removeLink(LinkEntity link) {
        links.remove(link);
        link.setChat(null);
    }
}
