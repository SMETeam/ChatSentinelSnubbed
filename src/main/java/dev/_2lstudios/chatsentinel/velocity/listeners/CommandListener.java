package dev._2lstudios.chatsentinel.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import dev._2lstudios.chatsentinel.velocity.ChatSentinel;
import dev._2lstudios.chatsentinel.shared.chat.ChatEventResult;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.velocity.modules.VelocityModuleManager;

import java.util.Set;

public class CommandListener {

    private final ChatSentinel plugin;
    private final VelocityModuleManager moduleManager;

    public CommandListener(ChatSentinel plugin) {
        this.plugin = plugin;
        this.moduleManager = plugin.getModuleManager();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onCommand(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getCommandSource();

        if (player.hasPermission("chatsentinel.bypass")) {
            return;
        }

        String command = event.getCommand();
        String[] parts = command.split(" ");

        String baseCommand = parts[0].toLowerCase();

        if (baseCommand.startsWith("/")) {
            baseCommand = baseCommand.substring(1);
        }

        if (baseCommand.equals("report")) {
            return; // Ignore /report command entirely so then they can still report censored messages
        }

        // No arguments at all
        if (parts.length < 2) {
            return;
        }

        Set<String> userTargetCommands = moduleManager != null ? moduleManager.getUserTargetCommands() : Set.of();

        String prefix;
        String message;

        if (userTargetCommands.contains(baseCommand) && parts.length >= 3) {
            prefix = parts[0] + " " + parts[1];
            message = joinFrom(parts, 2);
        } else {
            prefix = parts[0];
            message = joinFrom(parts, 1);
        }

        ChatPlayer chatPlayer = plugin.getChatPlayerManager().getPlayer(player);
        ChatEventResult result = plugin.processCommandEvent(chatPlayer, player, message);

        if (result.isCancelled()) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        event.setResult(CommandExecuteEvent.CommandResult.command(
                prefix + " " + result.getMessage()
        ));
    }

    private static String joinFrom(String[] parts, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < parts.length; i++) {
            if (i > start) sb.append(' ');
            sb.append(parts[i]);
        }
        return sb.toString();
    }
}
