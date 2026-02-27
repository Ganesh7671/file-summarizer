package com.example.filesummarizer.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.filesummarizer.exception.FileProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;


import java.util.*;


/**
 * Calls the local Ollama AI server to generate summaries and image descriptions.
 *
 * Text summarization uses: llama3
 * Image description uses:  llava (multimodal model)
 *
 * Ollama must be running locally: ollama serve
 */
@Service
public class AiService {


    private final WebClient ollamaWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Value("${app.ollama.text-model}")
    private String textModel;


    @Value("${app.ollama.image-model}")
    private String imageModel;


    public AiService(WebClient ollamaWebClient) {
        this.ollamaWebClient = ollamaWebClient;
    }


    /**
     * Sends extracted document text to Ollama llama3 and returns a summary.
     */
    public String summarizeText(String text) {
        String prompt = "Please provide a clear and concise summary of the following document. " +
                "Focus on the main points and key information:\n\n" + text;


        // Use LinkedHashMap to guarantee field order in JSON serialization
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", textModel);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);


        return callOllama(requestBody);
    }


    /**
     * Sends an image as base64 to Ollama llava model for description.
     *
     * IMPORTANT: llava requires:
     *   - "model": "llava"
     *   - "prompt": a text prompt
     *   - "images": a JSON array of base64-encoded strings (NOT data URIs, raw base64 only)
     *   - "stream": false
     *
     * Do NOT include "data:image/png;base64," prefix — raw base64 only.
     *
     * @param imageBytes Raw bytes of the image file
     * @return AI-generated description
     */
    public String describeImage(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return "No image data available to describe.";
        }


        // Raw base64 — NO data URI prefix, llava handles the format itself
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);


        String prompt = "Describe this image in detail. What objects, people, text, colors, " +
                "and context do you see? Be thorough and specific.";


        // LinkedHashMap preserves insertion order — important for correct JSON structure
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", imageModel);
        requestBody.put("prompt", prompt);
        requestBody.put("images", List.of(base64Image)); // llava expects a List<String>
        requestBody.put("stream", false);


        return callOllama(requestBody);
    }


    /**
     * Makes the HTTP POST to Ollama /api/generate.
     * Blocks synchronously since we need the result before returning.
     */
    @SuppressWarnings("unchecked")
    private String callOllama(Map<String, Object> requestBody) {
        try {
            Map<String, Object> response = ollamaWebClient
                    .post()
                    .uri("/api/generate")
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();


            if (response == null) {
                return "AI returned an empty response.";
            }


            // Ollama returns the result in the "response" field
            Object result = response.get("response");
            if (result == null || result.toString().isBlank()) {
                return "AI generated an empty summary. The model may need more context.";
            }


            return result.toString().trim();


        } catch (WebClientRequestException e) {
            System.err.println("⚠️ Ollama not reachable: " + e.getMessage());
            return "⚠️ AI service unavailable. Ensure Ollama is running: `ollama serve`";


        } catch (Exception e) {
            System.err.println("⚠️ AI call failed: " + e.getMessage());
            return "⚠️ AI summary failed: " + e.getMessage();
        }
    }
}


