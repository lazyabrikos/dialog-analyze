package ru.dialog.analyzer.gptclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.dialog.analyzer.exceptions.HttpException;
import ru.dialog.analyzer.exceptions.NotAvailableException;
import ru.dialog.analyzer.model.ChatGPTRequest;
import ru.dialog.analyzer.model.ChatGPTResponse;

import java.util.Optional;

@Component
@Configuration
@Slf4j
public class ChatGPTClient {

    @Qualifier("chatGPTRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    public Optional<String> chat(String prompt) {
        ChatGPTRequest request = new ChatGPTRequest(model, prompt);

        log.info("Sending request with body = {}", request);
        try {
            ChatGPTResponse response = restTemplate.postForObject(apiUrl, request, ChatGPTResponse.class);

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.warn("Got empty response from ChatGPT");
                return Optional.empty();
            }

            return Optional.of(response.getChoices().getFirst().getMessage().getContent());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error from ChatGPT API: status = {}, body = {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new HttpException("Error while requesting Chat GPT API");
        } catch (ResourceAccessException e) {
            log.error("ChatGPT API not available: {}", e.getMessage());
            throw new NotAvailableException("ChatGPT API not available");
        } catch (Exception e) {
            log.error("Unknown error while requesting ChatGPT API", e);
            throw new RuntimeException("Unknown error while requesting ChatGPT API");
        }

    }
}
