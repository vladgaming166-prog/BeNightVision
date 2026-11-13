package ro.besina.benightvision.config;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ro.besina.benightvision.BeNightVision;

public final class PluginSettings {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final BeNightVision plugin;

    private String prefix;
    private boolean globalNightVision;
    private String noPermission;
    private String enabled;
    private String disabled;
    private String globalEnabled;
    private String globalDisabled;
    private String playerNotFound;
    private String playerOffline;
    private String usage;
    private String usageOn;
    private String usageOff;
    private List<String> help;

    public PluginSettings(BeNightVision plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();
        this.prefix = value(config, "prefix", "&8[&bBeNightVision&8]&r ");
        this.globalNightVision = config.getBoolean("global-night-vision", false);
        this.noPermission = value(config, "messages.no-permission", "&cYou do not have permission to do that.");
        this.enabled = value(config, "messages.enabled", "&aNight Vision enabled for &e{player}&a.");
        this.disabled = value(config, "messages.disabled", "&cNight Vision disabled for &e{player}&c.");
        this.globalEnabled = value(config, "messages.global-enabled", "&aNight Vision enabled for all players.");
        this.globalDisabled = value(config, "messages.global-disabled", "&cNight Vision disabled for all players.");
        this.playerNotFound = value(config, "messages.player-not-found", "&cPlayer &e{player} &cwas not found.");
        this.playerOffline = value(config, "messages.player-offline", "&cPlayer &e{player} &cis not online.");
        this.usage = value(config, "messages.usage", "&eUsage: &f/bnv <on|off> <player|all>");
        this.usageOn = value(config, "messages.usage-on", "&eUsage: &f/bnv on <player|all>");
        this.usageOff = value(config, "messages.usage-off", "&eUsage: &f/bnv off <player|all>");
        this.help = config.getStringList("messages.help");
        if (this.help.isEmpty()) {
            this.help = List.of(
                    "&b&lBeNightVision",
                    "&e/bnv on all &8- &7Enable Night Vision for every online player",
                    "&e/bnv on <player> &8- &7Enable Night Vision for a specific player",
                    "&e/bnv off all &8- &7Disable Night Vision for every online player",
                    "&e/bnv off <player> &8- &7Disable Night Vision for a specific player"
            );
        }
    }

    public boolean isGlobalNightVision() {
        return globalNightVision;
    }

    public void setGlobalNightVision(boolean enabled) {
        this.globalNightVision = enabled;
        plugin.getConfig().set("global-night-vision", enabled);
        plugin.saveConfig();
    }

    public void sendNoPermission(CommandSender sender) {
        send(sender, noPermission);
    }

    public void sendEnabled(CommandSender sender, String playerName) {
        send(sender, enabled, playerName);
    }

    public void sendDisabled(CommandSender sender, String playerName) {
        send(sender, disabled, playerName);
    }

    public void sendGlobalEnabled(CommandSender sender) {
        send(sender, globalEnabled);
    }

    public void sendGlobalDisabled(CommandSender sender) {
        send(sender, globalDisabled);
    }

    public void sendPlayerNotFound(CommandSender sender, String playerName) {
        send(sender, playerNotFound, playerName);
    }

    public void sendPlayerOffline(CommandSender sender, String playerName) {
        send(sender, playerOffline, playerName);
    }

    public void sendUsage(CommandSender sender) {
        send(sender, usage);
    }

    public void sendUsageOn(CommandSender sender) {
        send(sender, usageOn);
    }

    public void sendUsageOff(CommandSender sender) {
        send(sender, usageOff);
    }

    public void sendHelp(CommandSender sender) {
        for (String line : help) {
            send(sender, line);
        }
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(component(prefix + message));
    }

    private void send(CommandSender sender, String message, String playerName) {
        send(sender, message.replace("{player}", playerName));
    }

    private static Component component(String message) {
        return LEGACY.deserialize(message);
    }

    private static String value(FileConfiguration config, String path, String fallback) {
        String value = config.getString(path);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
