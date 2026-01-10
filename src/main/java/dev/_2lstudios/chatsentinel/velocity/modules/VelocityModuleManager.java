package dev._2lstudios.chatsentinel.velocity.modules;

import dev._2lstudios.chatsentinel.velocity.utils.ConfigUtil;
import dev._2lstudios.chatsentinel.shared.modules.ModuleManager;
import dev._2lstudios.chatsentinel.shared.webhook.DiscordWebhookEmbedSettings;
import dev._2lstudios.chatsentinel.shared.webhook.DiscordWebhookSettings;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class VelocityModuleManager extends ModuleManager {
	private final ConfigUtil configUtil;
	private Set<String> userTargetCommands = new HashSet<>();

	public VelocityModuleManager(ConfigUtil configUtil) {
		this.configUtil = configUtil;
		reloadData();
	}

	@Override
	public void reloadData() {
		configUtil.create("config.yml");
		configUtil.create("messages.yml");
		configUtil.create("blacklist.yml");
		configUtil.create("whitelist.yml");
		configUtil.create("commands.yml");

		CommentedConfigurationNode blacklistYml = configUtil.get("blacklist.yml");
		CommentedConfigurationNode configYml = configUtil.get("config.yml");
		CommentedConfigurationNode messagesYml = configUtil.get("messages.yml");
		CommentedConfigurationNode whitelistYml = configUtil.get("whitelist.yml");
		CommentedConfigurationNode commandsYml = configUtil.get("commands.yml");
		Map<String, Map<String, String>> locales = new HashMap<>();

		for (Object lang : messagesYml.node("langs").childrenMap().keySet()) {
			ConfigurationNode langSection = messagesYml.node("langs", lang);
			Map<String, String> messages = new HashMap<>();

			for (Object key : langSection.childrenMap().keySet()) {
				String value = langSection.node(key).getString();

				messages.put((String) key, value);
			}

			locales.put((String) lang, messages);
		}

		getCapsModule().loadData(configYml.node("caps", "enabled").getBoolean(),
				configYml.node("caps", "replace").getBoolean(),
				configYml.node("caps", "max").getInt(), configYml.node("caps", "warn", "max").getInt(),
				configYml.node("caps", "warn", "notification").getString(),
				configYml.node("caps", "punishments").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new));
		if (commandsYml != null) {
			userTargetCommands = commandsYml.node("user-target-commands").childrenList().stream()
					.map(ConfigurationNode::getString)
					.filter(Objects::nonNull)
					.map(String::toLowerCase)
					.collect(Collectors.toCollection(HashSet::new));
		} else {
			userTargetCommands = new HashSet<>();
		}
		getCapsModule().loadData(configYml.node("caps", "enabled").getBoolean(),
				configYml.node("caps", "replace").getBoolean(),
				configYml.node("caps", "max").getInt(), configYml.node("caps", "warn", "max").getInt(),
				configYml.node("caps", "warn", "notification").getString(),
				configYml.node("caps", "punishments").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new));
		getCooldownModule().loadData(configYml.node("cooldown", "enabled").getBoolean(),
				configYml.node("cooldown", "time", "repeat-global").getInt(),
				configYml.node("cooldown", "time", "repeat").getInt(),
				configYml.node("cooldown", "time", "normal").getInt(),
				configYml.node("cooldown", "time", "command").getInt());
		getFloodModule().loadData(configYml.node("flood", "enabled").getBoolean(),
				configYml.node("flood", "replace").getBoolean(),
				configYml.node("flood", "warn", "max").getInt(), configYml.node("flood", "pattern").getString(),
				configYml.node("flood", "warn", "notification").getString(),
				configYml.node("flood", "punishments").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new));
		getMessagesModule().loadData(messagesYml.node("default").getString(), locales);
		getGeneralModule().loadData(configYml.node("general", "sanitize").getBoolean(true),
				configYml.node("general", "sanitize-names").getBoolean(true),
				configYml.node("general", "filter-other").getBoolean(false),
				configYml.node("general", "commands").childrenList().stream()
						.map(ConfigurationNode::getString)
						.collect(Collectors.toList()));
		getWhitelistModule().loadData(configYml.node("whitelist", "enabled").getBoolean(),
				whitelistYml.node("expressions").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new));
		boolean censorshipEnabled = configYml.node("blacklist", "censorship", "enabled").getBoolean(false);
		String censorshipReplacement = configYml.node("blacklist", "censorship", "replacement").getString("***");
		getBlacklistModule().loadData(configYml.node("blacklist", "enabled").getBoolean(),
				configYml.node("blacklist", "fake_message").getBoolean(),
				censorshipEnabled,
				censorshipReplacement,
				configYml.node("blacklist", "warn", "max").getInt(),
				configYml.node("blacklist", "warn", "notification").getString(),
				configYml.node("blacklist", "punishments").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new),
				blacklistYml.node("expressions").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new),
				configYml.node("blacklist", "block_raw_message").getBoolean());
		getSyntaxModule().loadData(configYml.node("syntax", "enabled").getBoolean(),
				configYml.node("syntax", "warn", "max").getInt(),
				configYml.node("syntax", "warn", "notification").getString(),
				configYml.node("syntax", "whitelist").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new),
				configYml.node("syntax", "punishments").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new));

		CommentedConfigurationNode webhookNode = configYml.node("discord", "webhook");
		boolean webhookEnabled = webhookNode.node("enabled").getBoolean(false);
		String webhookUrl = webhookNode.node("url").getString("");
		String webhookUsername = webhookNode.node("username").getString("ChatSentinel");
		String webhookAvatarUrl = webhookNode.node("avatar_url").getString("");
		String webhookContent = webhookNode.node("content").getString("");

		CommentedConfigurationNode embedNode = webhookNode.node("embed");
		boolean embedEnabled = embedNode.node("enabled").getBoolean(true);
		String embedTitle = embedNode.node("title").getString("ChatSentinel Filter Flagged");
		String embedDescription = embedNode.node("description").getString("");
		int embedColor = DiscordWebhookEmbedSettings.PassColorPlease(embedNode.node("color").getString("FF5555"));
		boolean embedTimestamp = embedNode.node("timestamp").getBoolean(true);
		String embedFooterText = embedNode.node("footer", "text").getString("");
		String embedFooterIcon = embedNode.node("footer", "icon_url").getString("");

		CommentedConfigurationNode fieldsNode = embedNode.node("fields");
		boolean fieldsEnabled = fieldsNode.node("enabled").getBoolean(true);
		String fieldPlayerName = fieldsNode.node("player").getString("Player");
		String fieldDetectedName = fieldsNode.node("detected").getString("Detected");
		String fieldOriginalName = fieldsNode.node("original").getString("Original");
		String fieldCensoredName = fieldsNode.node("censored").getString("Censored");

		DiscordWebhookEmbedSettings embedSettings = new DiscordWebhookEmbedSettings(embedEnabled, embedTitle,
				embedDescription, embedColor, embedTimestamp, embedFooterText, embedFooterIcon, fieldsEnabled,
				fieldPlayerName, fieldDetectedName, fieldOriginalName, fieldCensoredName);
		setDiscordWebhookSettings(new DiscordWebhookSettings(webhookEnabled, webhookUrl, webhookUsername,
				webhookAvatarUrl, webhookContent, embedSettings));
	}

	public Set<String> getUserTargetCommands() {
		return userTargetCommands;
	}
}
