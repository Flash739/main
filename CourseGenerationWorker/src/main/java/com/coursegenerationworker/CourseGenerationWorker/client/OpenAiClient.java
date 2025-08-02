package com.coursegenerationworker.CourseGenerationWorker.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiClient {

    private final WebClient webClient;
    private final String hfToken = "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";

    public OpenAiClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://router.huggingface.co").build();
    }
     public String getCourseOutlineFromLLM(String prompt) {
        try {
            Map response = webClient.post()
                    .uri("/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + hfToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "model", "deepseek-ai/DeepSeek-V3-0324",
                            "messages", List.of(
                                    Map.of("role", "user", "content", prompt)
                            )
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // sync call

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(response);
            JsonNode root = mapper.readTree(json);

            String content = root.path("choices").get(0).path("message").path("content").asText();

            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');

            if (start != -1 && end != -1 && end > start) {
                content = content.substring(start, end + 1);
            } else {
                throw new RuntimeException("Could not extract valid JSON from LLM response");
            }
            System.out.println(content);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
}
