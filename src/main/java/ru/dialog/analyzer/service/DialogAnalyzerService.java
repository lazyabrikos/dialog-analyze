package ru.dialog.analyzer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.dialog.analyzer.exceptions.EmptyAnswerException;
import ru.dialog.analyzer.exceptions.JsonException;
import ru.dialog.analyzer.gptclient.ChatGPTClient;
import ru.dialog.analyzer.model.Message;
import ru.dialog.analyzer.model.Response;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DialogAnalyzerService {

    @Value("${chatgpt.client.retries:5}")
    private int retries;

    private final ChatGPTClient chatGPTClient;

    private static final String promptTemplate = """
            У тебя есть диалог пользователя и бота: Начало диалога: %s Конец диалога. 
            Напиши краткое содержание диалога, оценку удовлетворенности пользователя ответом из слов 
            (доволен или не доволен) и напиши краткую оценку диалога по шкале от 1-5. 
            Ответь предоставь в JSON формате где summary - краткое содержание диалога, 
            satisfaction - удовлетворенность пользователя, rating - оценка по шкале от 1 до 5. 
            Если диалог пустой, то верни пустой ответ""";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public CompletableFuture<Response> analyzeDialog(List<Message> dialog) {
        StringBuilder dialogRequest = new StringBuilder();
        for (Message message : dialog) {
            dialogRequest.append(message.getRole()).append(": ").append(message.getContent()).append(" ");
        }

        String prompt = String.format(promptTemplate, dialogRequest.toString());
        String responseString = chatGPTClient.chat(prompt).orElseGet(() -> retryRequest(prompt));
        Response response = parseStringToResponse(responseString);

        return CompletableFuture.completedFuture(response);
    }

    public List<Response> analyze(List<List<Message>> messages) {
        List<CompletableFuture<Response>> futures = messages.stream()
                .map(this::analyzeDialog)
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Error while getting response from future", e);
                        throw new RuntimeException("Error while getting response from future", e);
                    }
                })
                .toList();

    }

    private Response parseStringToResponse(String responseString) {
        String cleaned = responseString
                .replaceAll("```json\\n?", "")
                .replaceAll("```", "");
        try {
            return objectMapper.readValue(cleaned, Response.class);
        } catch (JsonProcessingException e) {
            throw new JsonException("Error deserialize json");
        }
    }

    private String retryRequest(String prompt) {
        for (int i = 0; i < retries; i++) {
            Optional<String> responseString = chatGPTClient.chat(prompt);
            if (responseString.isPresent()) {
                return responseString.get();
            }
        }
        throw new EmptyAnswerException("Got empty or null answer from ChatGPT API");
    }
}
