package practice.usingai.api.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiApiService {

    //dotenv 의존성 주입 잘못함. dotenv 쓰레기임
//    @Autowired
//    private Environment environment;
//
//    @PostConstruct
//    public void checkEnv() {
//        System.out.println(environment.getProperty("OPENAI_API_KEY"));
//    }

    private final String API_KEY = Dotenv.load().get("OPENAI_API_KEY");

    @Value("${spring.ai.openai.chat.model}")
    private String model;


    public String getChatResponse(String message) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//        System.out.println("api키 : " + API_KEY);
        httpHeaders.set("Authorization", "Bearer "+API_KEY);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", message)));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, httpHeaders);

        ResponseEntity<Map> response = restTemplate.postForEntity("https://api.openai.com/v1/chat/completions", request, Map.class);

        List<Map<String,Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> responseMessage = (Map<String, Object>) choices.get(0).get("message");

        return responseMessage.get("message").toString();
    }


}
