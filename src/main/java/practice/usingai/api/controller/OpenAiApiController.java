package practice.usingai.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import practice.usingai.api.dto.ChatRequest;
import practice.usingai.api.dto.ChatResponse;
import practice.usingai.api.service.OpenAiApiService;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
//그냥 무지성 노답 원시코드
public class OpenAiApiController {

    private final OpenAiApiService openAiApiService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest chatRequest) {
        String response = openAiApiService.getChatResponse(chatRequest.getMessage());
        return new ChatResponse(response);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
