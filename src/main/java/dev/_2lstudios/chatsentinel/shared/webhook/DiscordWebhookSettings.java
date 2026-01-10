package dev._2lstudios.chatsentinel.shared.webhook;

public class DiscordWebhookSettings {
    private final boolean enabled;
    private final String url;
    private final String username;
    private final String avatarUrl;
    private final String content;
    private final DiscordWebhookEmbedSettings embed;

    public DiscordWebhookSettings(boolean enabled, String url, String username, String avatarUrl, String content,
            DiscordWebhookEmbedSettings embed) {
        this.enabled = enabled;
        this.url = url;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.content = content;
        this.embed = embed;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getContent() {
        return content;
    }

    public DiscordWebhookEmbedSettings getEmbed() {
        return embed;
    }
}
