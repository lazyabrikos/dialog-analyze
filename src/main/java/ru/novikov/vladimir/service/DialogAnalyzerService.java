package ru.novikov.vladimir.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import ru.novikov.vladimir.exceptions.EmptyAnswerException;
import ru.novikov.vladimir.exceptions.JsonException;
import ru.novikov.vladimir.gptclient.ChatGPTClient;
import ru.novikov.vladimir.model.Message;
import ru.novikov.vladimir.model.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DialogAnalyzerService {

    @Value("${chatgpt.client.retries}")
    private int retries;

    private final ChatGPTClient chatGPTClient;

    private static final String promptTemplate = "У тебя есть диалог пользователя и бота: Начало диалога: %s " +
            "Конец диалога" +
            "Напиши краткое содержание диалога, оценку удовлетворенности пользователя ответом из слов " +
            "(доволен или не доволен) и напиши краткую оценку диалога по шкале от 1-5. " +
            "Ответь предоставь в JSON формате где summary - краткое содержание диалога, " +
            "satisfaction - удовлетворенность пользователя, rating - оценка по шкале от 1 до 5. " +
            "Если диалог пустой, то верни пустой ответ";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<Response> analyze(List<List<Message>> messages) {
        List<Response> responses = new ArrayList<>();
        for (List<Message> dialog : messages) {
            StringBuilder dialogRequest = new StringBuilder();
            for (Message message : dialog) {
                dialogRequest.append(message.getRole()).append(": ").append(message.getContent()).append(" ");
            }

            String prompt = String.format(promptTemplate, dialogRequest.toString());
            String responseString = chatGPTClient.chat(prompt).orElseGet(() -> retryRequest(prompt));
            Response response = parseStringToResponse(responseString);
            responses.add(response);
        }
        return responses;
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
