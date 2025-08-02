package com.example.highpriority.highpriority.Client;

import com.example.highpriority.highpriority.model.CourseOutline;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class LlmClient {

    private final WebClient webClient;
    private final String hfToken = "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";
    private final ObjectMapper mapper = new ObjectMapper();

    public LlmClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://router.huggingface.co").build();
    }

    private String callLLM(String prompt) {
        try {
            Map<String, Object> response = webClient.post()
                    .uri("/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + hfToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "model", "deepseek-ai/DeepSeek-V3-0324",
                            "messages", List.of(Map.of("role", "user", "content", prompt))
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String json = mapper.writeValueAsString(response);
            JsonNode root = mapper.readTree(json);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');

            if (start != -1 && end != -1 && end > start) {
                return content.substring(start, end + 1);
            }

            return content;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public String generateSectionDescription(String topic, String sectionTitle) {
        String prompt = String.format(
                "You are an expert educational content writer. Your job is to write a rich, complete, and flowing section for the textbook section titled '%s', part of a course on '%s'.\n\n" +
                        "Write a long-form explanation in plain text only, no JSON or structured output. Follow these instructions:\n" +
                        "1. Use **bolded headings** for major ideas (Markdown-style).\n" +
                        "2. Use clean paragraphs with clear intuitions, analogies, and examples.\n" +
                        "3. Define all concepts step by step.\n" +
                        "4. Do not include lists, code, or bullet points.\n" +
                        "5. After each paragraph, add a parenthetical link to a credible source.\n" +
                        "6. End with a short summary.\n\n" +
                        "Write the full section content for '%s'. Output should be plain text only.",
                sectionTitle, topic, sectionTitle
        );
        return callLLM(prompt);
    }



    public List<CourseOutline.Video> generateSectionVideos(String topic, String sectionTitle) {
        String prompt = String.format("Suggest educational YouTube videos (videos so that user can watch them and undestand throughly)  titles and URLs for a section titled '%s' in a course about '%s'. Respond in JSON like: [{\"title\": \"\", \"url\": \"\"}]", sectionTitle, topic);
        try {
            String json = callLLM(prompt);
            JsonNode root = mapper.readTree(json);

// If it's a single object, wrap it in a list
            if (root.isObject()) {
                CourseOutline.Video video = mapper.treeToValue(root, CourseOutline.Video.class);
                return List.of(video);
            } else if (root.isArray()) {
                CourseOutline.Video[] videos = mapper.treeToValue(root, CourseOutline.Video[].class);
                return Arrays.asList(videos);
            } else {
                throw new IllegalStateException("Unexpected response format: " + root.toString());
            }
           // return Arrays.asList(mapper.readValue(json, CourseOutline.Video[].class));
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }


    public List<String> getSubsectionTitles(String topic, String sectionTitle) {
        String prompt = String.format("List 3 to 5 subsection titles for a section '%s' in a course on '%s'. Return as JSON list.", sectionTitle, topic);
        try {
            String json = callLLM(prompt);

            int start = json.indexOf('[');
            int end = json.lastIndexOf(']');
            String content="";

            if (start != -1 && end != -1 && end > start) {
                content=json.substring(start, end + 1);
            }
            System.out.println(json+"1"+sectionTitle);
            ObjectMapper mapper = new ObjectMapper();

            List<String> titles = mapper.readValue(content, new TypeReference<List<String>>() {});
            return titles;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
/*
    public String generateSubsectionContent(String topic, String sectionTitle, String subsectionTitle) {
        String prompt = String.format(
                "You are a highly skilled educator writing clear and complete tutorial content.\n\n" +
                        "Write a long, beginner-friendly, and self-contained explanation for the subsection titled '%s', " +
                        "which belongs to the section '%s' in a course on '%s'.\n\n" +
                        "Guidelines:\n" +
                        "1. Use simple, clear language and write in natural, textbook-style paragraphs.\n" +
                        "2. Start with an introduction to the concept, then build up gradually.\n" +
                        "3. Explain all terms and ideas from scratch, with examples and analogies if helpful.\n" +
                        "4. You may include links to trusted resources (e.g., towardsdatascience.com, deeplearning.ai, etc.) only where the reader might want further reading.\n" +
                        "5. Do not use bullet points or markdown formatting (no **bold**, no // comments, no headings, etc.).\n" +
                        "6. Just use plain text structured into multiple readable paragraphs.\n" +
                        "7. End with a 2–4 line summary to reinforce what the user learned.\n\n" +
                        "Now write the content for the subsection.",
                subsectionTitle, sectionTitle, topic
        );
        return callLLM(prompt);
    }*/

    public String generateSubsectionContent(String topic, String sectionTitle, String subsectionTitle) {
        String prompt = String.format(
                "You are a highly skilled educator writing clear and complete tutorial content.\n\n" +
                        "Write a long, beginner-friendly, and self-contained explanation for the subsection titled '%s', " +
                        "which belongs to the section '%s' in a course on '%s'.\n\n" +
                        "Guidelines:\n" +
                        "1. Use simple, clear language and write in natural, textbook-style paragraphs.\n" +
                        "2. Start with an introduction to the concept, then build up gradually.\n" +
                        "3. Explain all terms and ideas from scratch, with examples and analogies if helpful.\n" +
                        "4. Include direct URLs to trusted resources only when relevant (e.g., https://towardsdatascience.com, https://deeplearning.ai, https://en.wikipedia.org, etc.) so that readers can explore more.\n" +
                        "5. Do not use bullet points, markdown formatting, bold, headings, or special characters like ** or //.\n" +
                        "6. Write everything as plain text, structured into multiple readable paragraphs.\n" +
                        "7. End with a 2–4 line summary to reinforce what the user learned.\n\n" +
                        "Now write the content for the subsection.",
                subsectionTitle, sectionTitle, topic
        );
        return callLLM(prompt);
    }




    public List<CourseOutline.Quiz> generateSubsectionQuiz(String topic, String sectionTitle, String subsectionTitle) {
        String prompt = String.format(
                "Generate 10 multiple-choice questions (MCQs) for the topic '%s' in the '%s' section of a '%s' course. " +
                        "Each question should have 4 options and indicate the correct option using an integer index (0-based). " +
                        "Return ONLY a JSON array like Please give array with []: " +
                        "[{\"question\": \"...\", \"options\": [\"A\", \"B\", \"C\", \"D\"], \"correctOptionIndex\": 2}]",
                subsectionTitle, sectionTitle, topic
        );

        String json = callLLM(prompt);
        String formatstring= "["+json+"]";
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(formatstring);


            if (root.isArray()) {
                return Arrays.asList(mapper.treeToValue(root, CourseOutline.Quiz[].class));
            }  else {
                throw new IllegalArgumentException("Unexpected JSON format from LLM");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse quiz from LLM: " + json, e);
        }
    }

    public List<CourseOutline.Video> generateSubsectionVideos(String topic, String sectionTitle, String subsectionTitle) {
        String prompt = String.format(
                "Suggest 5 high-quality educational YouTube videos that help users thoroughly understand the topic of '%s', which is a subsection of '%s' in a '%s' course. " +
                        "Each suggestion should include: " +
                        "\"title\" (video title), " +
                        "\"url\" (YouTube link), and " +
                        "\"description\" (1-2 line summary of the video's content). " +
                        "Respond with ONLY a JSON array in the following format please give array of objects: " +
                        "[{\"title\": \"...\", \"url\": \"https://...\", \"description\": \"...\"}]",
                subsectionTitle, sectionTitle, topic
        );

            String json = callLLM(prompt);
        String formatstring= "["+json+"]";
            // returns string

        System.out.println(json);

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(formatstring);


                if (root.isArray()) {
                    return Arrays.asList(mapper.treeToValue(root, CourseOutline.Video[].class));
                }  else {
                    throw new IllegalArgumentException("Unexpected JSON format from LLM");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse quiz from LLM: " + json, e);
            }
        }
    }
