package dev._2lstudios.chatsentinel.shared.webhook;

public class DiscordWebhookEmbedSettings {
    private final boolean enabled;
    private final String title;
    private final String description;
    private final int color;
    private final boolean timestamp;
    private final String footerText;
    private final String footerIconUrl;
    private final boolean fieldsEnabled;
    private final String fieldPlayerName;
    private final String fieldDetectedName;
    private final String fieldOriginalName;
    private final String fieldCensoredName;

    public DiscordWebhookEmbedSettings(boolean enabled, String title, String description, int color, boolean timestamp,
            String footerText, String footerIconUrl, boolean fieldsEnabled, String fieldPlayerName,
            String fieldDetectedName, String fieldOriginalName, String fieldCensoredName) {
        this.enabled = enabled;
        this.title = title;
        this.description = description;
        this.color = color;
        this.timestamp = timestamp;
        this.footerText = footerText;
        this.footerIconUrl = footerIconUrl;
        this.fieldsEnabled = fieldsEnabled;
        this.fieldPlayerName = fieldPlayerName;
        this.fieldDetectedName = fieldDetectedName;
        this.fieldOriginalName = fieldOriginalName;
        this.fieldCensoredName = fieldCensoredName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getColor() {
        return color;
    }

    public boolean isTimestamp() {
        return timestamp;
    }

    public String getFooterText() {
        return footerText;
    }

    public String getFooterIconUrl() {
        return footerIconUrl;
    }

    public boolean isFieldsEnabled() {
        return fieldsEnabled;
    }

    public String getFieldPlayerName() {
        return fieldPlayerName;
    }

    public String getFieldDetectedName() {
        return fieldDetectedName;
    }

    public String getFieldOriginalName() {
        return fieldOriginalName;
    }

    public String getFieldCensoredName() {
        return fieldCensoredName;
    }

    public static int PassColorPlease(String colorValue) {
        if (colorValue == null) {
            return -1;
        }

        String trimmed = colorValue.trim();
        if (trimmed.isEmpty()) {
            return -1;
        }

        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }

        if (trimmed.startsWith("0x") || trimmed.startsWith("0X")) {
            trimmed = trimmed.substring(2);
        }

        try {
            return Integer.parseInt(trimmed, 16);
        } catch (NumberFormatException ignored) {
            try {
                return Integer.parseInt(trimmed);
            } catch (NumberFormatException ignoredAgain) {
                return -1;
            }
        }
    }
}
