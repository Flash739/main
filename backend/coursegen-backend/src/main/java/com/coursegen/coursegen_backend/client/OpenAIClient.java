package com.coursegen.coursegen_backend.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class OpenAIClient {

    //@Value("${huggingface.api.key}")
    private final WebClient webClient;

  //  @Value("${huggingface.token}")
    private String hfToken="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";

    public OpenAIClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://router.huggingface.co").build();
    }

    public Mono<String> extractFromLLM(String prompt) {
        return webClient.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + hfToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", "deepseek-ai/DeepSeek-V3-0324",  // Replace with your model
                        "messages", List.of(
                                Map.of("role", "user", "content", prompt)
                        )
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    System.out.println(response);
                    ObjectMapper mapper = new ObjectMapper();

                    String cleaned = null;

                    try {
                        // Convert the Map to JSON string
                        String json = mapper.writeValueAsString(response);
                        JsonNode root = mapper.readTree(json);

                        String content = root.path("choices").get(0).path("message").path("content").asText(null);
                        String[] parts = content.split("\\*\\*");

                        if (parts.length >= 2) {
                            String afterStars = parts[1]; // this will be: "'create-course'"

                            // Optional: remove surrounding single quotes
                            cleaned = afterStars.replaceAll("^'+|'+$", ""); // removes leading/trailing '

                            System.out.println("Extracted: " + cleaned); // Output: create-course
                        }

                        return cleaned;
                    } catch (Exception e) {
                       // e.printStackTrace();
                        return "Parsing error";
                    }
                });

    }
}
