package dev._2lstudios.chatsentinel.shared.modules;

import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev._2lstudios.chatsentinel.shared.chat.ChatEventResult;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.utils.PatternUtil;

public class BlacklistModerationModule extends ModerationModule {
	private ModuleManager moduleManager;

	private boolean fakeMessage;
  	private boolean blockRawMessage;
	private Pattern pattern;

	private boolean censorshipEnabled;
	private String censorshipReplacement;

	public BlacklistModerationModule(ModuleManager moduleManager) {
		this.moduleManager = moduleManager;
	}

	public void loadData(boolean enabled, boolean fakeMessage, boolean censorshipEnabled, String censorshipReplacement, int maxWarns,
        String warnNotification, String[] commands, String[] patterns, boolean blockRawMessage) {
		setEnabled(enabled);
		setMaxWarns(maxWarns);
		setWarnNotification(warnNotification);
		setCommands(commands);
		this.fakeMessage = fakeMessage;
		this.censorshipEnabled = censorshipEnabled;
		this.censorshipReplacement = censorshipReplacement;
		this.pattern = PatternUtil.compile(patterns);
		this.blockRawMessage = blockRawMessage;
	}

	public boolean isFakeMessage() {
		return this.fakeMessage;
	}

	public boolean isCensorshipEnabled() {
		return censorshipEnabled;
	}

	public String getCensorshipReplacement() {
		return censorshipReplacement;
	}

	public boolean isBlockRawMessage() {
		return this.blockRawMessage;
	}

	public Pattern getPattern() {
		return pattern;
	}

	@Override
	public ChatEventResult processEvent(ChatPlayer chatPlayer, MessagesModule messagesModule, String playerName,
			String message, String lang) {
		if (!isEnabled()) {
			return null;
		}

		boolean cancelled = false;
		boolean hide = false;

		GeneralModule generalModule = moduleManager.getGeneralModule();
		WhitelistModule whitelistModule = moduleManager.getWhitelistModule();

		String sanitizedMessage = message;

		// Remove the command part
		if (sanitizedMessage.startsWith("/") && message.contains(" ")) {
			sanitizedMessage = sanitizedMessage.substring(message.indexOf(" "));
		}

		// Santize it after
		if (generalModule.isSanitizeEnabled()) {
			sanitizedMessage = generalModule.sanitize(message);
		}

		// Then if theres a name remove it
		if (generalModule.isSanitizeNames()) {
			sanitizedMessage = generalModule.sanitizeNames(message);
		}

		// Also clear the whitelist matches
		if (whitelistModule.isEnabled()) {
			sanitizedMessage = whitelistModule.getPattern().matcher(message).replaceAll("");
		}

		Matcher matcher = pattern.matcher(sanitizedMessage);
		if (matcher.find()) {
			LinkedHashSet<String> matches = new LinkedHashSet<>();
			do {
				String match = matcher.group();
				if (match != null && !match.isEmpty()) {
					matches.add(match);
				}
			} while (matcher.find());

			String matchedWord = String.join(", ", matches); // Join all matched words so like SWEAR, SWEAR etc hopefully
			if (isFakeMessage()) {
				hide = true;
			} else if (isCensorshipEnabled()) {
				message = pattern.matcher(message).replaceAll(getCensorshipReplacement());
			} else if (isBlockRawMessage()) {
				cancelled = true;
			}

			ChatEventResult result = new ChatEventResult(message, cancelled, hide);
			result.setMatchedWord(matchedWord);
			return result;
		}

		return null;
	}

	@Override
	public String getName() {
		return "Blacklist";
	}
}