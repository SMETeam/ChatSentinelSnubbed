package dev._2lstudios.chatsentinel.shared.modules;

public class AutoBlacklistModerationModule extends BlacklistModerationModule {
    public AutoBlacklistModerationModule(ModuleManager moduleManager) {
        super(moduleManager);
    }

    public void loadData(boolean enabled, String reason, String[] commands, String[] patterns) {
        setReason(reason);
        super.loadData(enabled, false, false, "", 1, "", commands, patterns, true);
    }

    @Override
    public String getName() {
        return "Blacklist";
    }
}
