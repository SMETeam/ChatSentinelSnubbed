package dev._2lstudios.chatsentinel.shared.webhook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public final class DiscordWebhookClient {
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;

    private DiscordWebhookClient() {
    }

    public static void send(DiscordWebhookSettings settings, DiscordWebhookMessage message) throws IOException {
        if (settings == null || !settings.isEnabled()) {
            return;
        }

        if (isBlank(settings.getUrl())) {
            return;
        }

        String payload = buildPayload(settings, message);
        if (isBlank(payload)) {
            return;
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(settings.getUrl()).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            byte[] data = payload.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(data.length);

            try (OutputStream output = connection.getOutputStream()) {
                output.write(data);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("Discord webhook returned HTTP " + responseCode);
            }

            InputStream response = connection.getInputStream();
            if (response != null) {
                response.close();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String buildPayload(DiscordWebhookSettings settings, DiscordWebhookMessage message) {
        String content = applyPlaceholders(settings.getContent(), message);
        String username = applyPlaceholders(settings.getUsername(), message);
        String avatarUrl = applyPlaceholders(settings.getAvatarUrl(), message);
        String embedJson = buildEmbed(settings.getEmbed(), message);

        if (isBlank(content) && embedJson == null) {
            return null;
        }

        StringBuilder json = new StringBuilder();
        json.append('{');
        boolean first = true;

        first = appendStringField(json, "username", username, first);
        first = appendStringField(json, "avatar_url", avatarUrl, first);
        first = appendStringField(json, "content", content, first);

        if (embedJson != null) {
            if (!first) {
                json.append(',');
            }
            json.append("\"embeds\":[");
            json.append(embedJson);
            json.append(']');
            first = false;
        }

        json.append('}');
        return json.toString();
    }

    private static String buildEmbed(DiscordWebhookEmbedSettings settings, DiscordWebhookMessage message) {
        if (settings == null || !settings.isEnabled()) {
            return null;
        }

        String title = applyPlaceholders(settings.getTitle(), message);
        String description = applyPlaceholders(settings.getDescription(), message);
        String footerText = applyPlaceholders(settings.getFooterText(), message);
        String footerIconUrl = applyPlaceholders(settings.getFooterIconUrl(), message);

        String fieldsJson = buildFields(settings, message);

        StringBuilder embed = new StringBuilder();
        embed.append('{');
        boolean first = true;

        first = appendStringField(embed, "title", title, first);
        first = appendStringField(embed, "description", description, first);

        if (settings.getColor() >= 0) {
            if (!first) {
                embed.append(',');
            }
            embed.append("\"color\":").append(settings.getColor());
            first = false;
        }

        if (fieldsJson != null) {
            if (!first) {
                embed.append(',');
            }
            embed.append("\"fields\":[");
            embed.append(fieldsJson);
            embed.append(']');
            first = false;
        }

        if (!isBlank(footerText) || !isBlank(footerIconUrl)) {
            if (!first) {
                embed.append(',');
            }
            embed.append("\"footer\":{");
            boolean footerFirst = true;
            footerFirst = appendStringField(embed, "text", footerText, footerFirst);
            footerFirst = appendStringField(embed, "icon_url", footerIconUrl, footerFirst);
            embed.append('}');
            first = false;
        }

        if (settings.isTimestamp()) {
            if (!first) {
                embed.append(',');
            }
            embed.append("\"timestamp\":\"").append(Instant.now().toString()).append('"');
            first = false;
        }

        embed.append('}');
        return first ? null : embed.toString();
    }

    private static String buildFields(DiscordWebhookEmbedSettings settings, DiscordWebhookMessage message) {
        if (!settings.isFieldsEnabled()) {
            return null;
        }

        String playerValue = safe(message.getPlayerName());
        String detectedValue = getDetectedWord(message);
        String originalValue = safe(message.getOriginalMessage());
        String censoredValue = safe(message.getCensoredMessage());

        String playerName = applyPlaceholders(settings.getFieldPlayerName(), message);
        String detectedName = applyPlaceholders(settings.getFieldDetectedName(), message);
        String originalName = applyPlaceholders(settings.getFieldOriginalName(), message);
        String censoredName = applyPlaceholders(settings.getFieldCensoredName(), message);

        StringBuilder fields = new StringBuilder();
        boolean first = true;

        first = appendField(fields, playerName, playerValue, true, first);
        first = appendField(fields, detectedName, detectedValue, true, first);
        first = appendField(fields, originalName, originalValue, false, first);
        first = appendField(fields, censoredName, censoredValue, false, first);

        return first ? null : fields.toString();
    }

    private static boolean appendStringField(StringBuilder json, String name, String value, boolean first) {
        if (isBlank(value)) {
            return first;
        }

        if (!first) {
            json.append(',');
        }
        json.append('"').append(name).append("\":\"").append(escapeJson(value)).append('"');
        return false;
    }

    private static boolean appendField(StringBuilder json, String name, String value, boolean inline, boolean first) {
        if (isBlank(name) || isBlank(value)) {
            return first;
        }

        if (!first) {
            json.append(',');
        }
        json.append('{');
        json.append("\"name\":\"").append(escapeJson(name)).append("\",");
        json.append("\"value\":\"").append(escapeJson(value)).append("\",");
        json.append("\"inline\":").append(inline);
        json.append('}');
        return false;
    }

    private static String applyPlaceholders(String template, DiscordWebhookMessage message) {
        if (template == null) {
            return null;
        }

        String player = safe(message.getPlayerName());
        String original = safe(message.getOriginalMessage());
        String censored = safe(message.getCensoredMessage());
        String detected = getDetectedWord(message);

        // place the placeholders
        return template
                .replace("%player%", player)
                .replace("%original%", original)
                .replace("%censored%", censored)
                .replace("%detected%", detected)
                .replace("%message%", original);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String getDetectedWord(DiscordWebhookMessage message) {
        String detected = message.getDetectedWord();
        return isBlank(detected) ? "unknown" : detected;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                    break;
            }
        }
        return escaped.toString();
    }
}
