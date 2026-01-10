package dev._2lstudios.chatsentinel.shared.webhook;

public class DiscordWebhookMessage {
    private final String playerName;
    private final String originalMessage;
    private final String censoredMessage;
    private final String detectedWord;

    public DiscordWebhookMessage(String playerName, String originalMessage, String censoredMessage, String detectedWord) {
        this.playerName = playerName;
        this.originalMessage = originalMessage;
        this.censoredMessage = censoredMessage;
        this.detectedWord = detectedWord;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public String getCensoredMessage() {
        return censoredMessage;
    }

    public String getDetectedWord() {
        return detectedWord;
    }
}
