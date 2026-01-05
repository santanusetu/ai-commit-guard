package com.aigitassist.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class SlackService {

    private final WebClient webClient;
    private final String webhookUrl;

    /**
     * Constructs SlackService with webhook URL from configuration.
     * @param webhookUrl Slack webhook URL from application.properties (empty if not configured)
     */
    public SlackService(@Value("${slack.webhook.url:}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Sends commit message notification to Slack channel via webhook.
     * Silently fails if webhook URL is not configured or request fails.
     * @param commitMessage The commit message to send
     */
    public void sendNotification(String commitMessage) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("text", "AI Commit:\n" + commitMessage);

        try {
            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            // Silently fail - Slack notification is optional
            System.err.println("Failed to send Slack notification: " + e.getMessage());
        }
    }
}

