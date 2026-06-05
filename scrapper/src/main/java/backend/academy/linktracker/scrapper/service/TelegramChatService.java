package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TelegramChatService {
    private final ChatRepository chatRepository;

    @Transactional
    public boolean registerChat(long chatId) {
        return chatRepository.registerChat(chatId);
    }

    @Transactional
    public boolean deleteChat(long chatId) {
        return chatRepository.deleteChat(chatId);
    }
}
