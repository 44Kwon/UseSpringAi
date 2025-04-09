package practice.usingai.libarary.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import practice.usingai.libarary.entity.ChatMessage;
import practice.usingai.libarary.service.ChatService;

@RestController
@RequestMapping("/api/v2/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity createChatMessage(@RequestParam String userId, @RequestParam String message) {
        String reply = chatService.chat(userId, message);
        return new ResponseEntity<>(reply, HttpStatus.OK);
    }
}
