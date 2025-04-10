package practice.usingai.libarary.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.usingai.libarary.entity.ChatMessage;
import practice.usingai.libarary.repository.ChatMessageRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public String chat(String userId, String message) {
        // 사용자 질문 저장
        chatMessageRepository.save(new ChatMessage(userId, ChatMessage.Role.USER, message));

        // 최근 대화 기록 40개 조회 및 정렬
        List<ChatMessage> sorted = chatMessageRepository.findTop40ByUserIdOrderByCreatedAtDesc(userId).stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))    //오름차순 정렬 -> 대화의 흐름을 알려주기 위해서
                .toList();

        //user - assistant 짝 맞추기
        List<ChatMessage> paired = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            ChatMessage curr = sorted.get(i);
            ChatMessage next = sorted.get(i + 1);
            if (curr.getRole() == ChatMessage.Role.USER && next.getRole() == ChatMessage.Role.USER) {
                paired.add(curr);
                paired.add(next);
            }
        }


        // 벡터 검색 기반 문서 검색
        List<Document> docs = vectorStore.similaritySearch(message).stream()
                .limit(3)
                .toList();

        String ragContext = docs.stream()
                .map(doc -> StringUtils.abbreviate(doc.getText(), 1000))    //1000자로 짤라냄
                .collect(Collectors.joining("\n"));//줄바꿈

        // GPT 대화 프롬프트 구성
        List<Message> prompt = new ArrayList<>();
        prompt.add(new SystemMessage("너는 친절한 도우미야. 아래 문서를 참고해서 답변해."));
        if (!ragContext.isEmpty()) {
            prompt.add(new SystemMessage("관련 문서: \n" + ragContext));
        }

        for (ChatMessage msg : paired) {
            prompt.add(switch (msg.getRole()) { //switch 표현식 break 필요X, 값을 알아서 반환함
                case USER -> new UserMessage(msg.getContent());
                case ASSISTANT -> new AssistantMessage(msg.getContent());
                case SYSTEM -> new SystemMessage(msg.getContent());
            });
        }
        prompt.add(new UserMessage(message));

        // GPT 호출 (테스트 해볼것)
        ChatClient.CallResponseSpec call = chatClient.prompt(new Prompt(prompt)).call();
        String reply = call.chatResponse().getResult().getOutput().getText();

        // 자동 요약 (길면)
        String summary = "";
        if (reply.length() > 300) {
            List<Message> summaryPrompt = List.of(
                    new SystemMessage("답변을 한두 문장으로 요약해줘."),     // 프롬프트 어케 할지...
                    new UserMessage("질문: " + message),
                    new AssistantMessage("답변: " + reply)
            );
            summary = chatClient.prompt(new Prompt(summaryPrompt)).call()
                    .chatResponse()
                    .getResult()
                    .getOutput()
                    .getText();
        }

        // 응답 저장 (요약 포함)
        ChatMessage assistantMessage = new ChatMessage(userId, ChatMessage.Role.ASSISTANT, reply);
        assistantMessage.setSummary(summary);
        chatMessageRepository.save(assistantMessage);

        if(!summary.isEmpty()) {
            return reply;
        }

        return summary;
    }
}
