package practice.usingai.libarary.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import practice.usingai.libarary.entity.ChatMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @DisplayName("유저의 Ai대화 기록 최신순으로 40개 가져오기")
    @Test
    void findTop40ByUserIdOrderByCreatedAtDesc() {
        //given
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            ChatMessage message = new ChatMessage("001", ChatMessage.Role.USER, "" + i);
            message.setCreatedAt(LocalDateTime.now().minusMinutes(i));
            messages.add(message);
        }
        chatMessageRepository.saveAll(messages);

        //when
        List<ChatMessage> result = chatMessageRepository.findTop40ByUserIdOrderByCreatedAtDesc("001");

        List<LocalDateTime> createdAtDesc = result.stream()
                .map(ChatMessage::getCreatedAt)
                .toList();


        //then
        assertThat(result).hasSize(40);
        assertThat(createdAtDesc).isSortedAccordingTo(Comparator.reverseOrder());   //해당 기준대로 정렬되어 있는지 검증

    }
}