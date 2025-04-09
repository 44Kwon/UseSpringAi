package practice.usingai.libarary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import practice.usingai.libarary.entity.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    //select * from chat_message c where c.user_id = :userId order by c.created_at desc limit 40
    List<ChatMessage> findTop40ByUserIdOrderByCreatedAtDesc(String userId);
}
