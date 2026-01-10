package dev._2lstudios.chatsentinel.shared.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatPlayerManager {
    private final Map<UUID, ChatPlayer> chatPlayers = new HashMap<>();

    public ChatPlayer getPlayer(UUID uuid) {
        return chatPlayers.computeIfAbsent(uuid, ChatPlayer::new);
    }

    public ChatPlayer getPlayer(com.velocitypowered.api.proxy.Player player) {
        return getPlayer(player.getUniqueId());
    }
}
