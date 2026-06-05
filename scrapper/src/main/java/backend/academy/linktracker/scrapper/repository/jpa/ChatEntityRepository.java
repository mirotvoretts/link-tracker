package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.domain.jpa.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatEntityRepository extends JpaRepository<ChatEntity, Long> {}
