package ro.besina.benightvision.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import ro.besina.benightvision.config.PluginSettings;
import ro.besina.benightvision.manager.NightVisionManager;

public final class NightVisionCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION_ADMIN = "benightvision.admin";
    private static final String PERMISSION_USE = "benightvision.use";
    private static final List<String> ACTIONS = List.of("on", "off");

    private final PluginSettings settings;
    private final NightVisionManager nightVisionManager;

    public NightVisionCommand(PluginSettings settings, NightVisionManager nightVisionManager) {
        this.settings = settings;
        this.nightVisionManager = nightVisionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!hasUsePermission(sender)) {
                settings.sendNoPermission(sender);
                return true;
            }
            settings.sendHelp(sender);
            return true;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        if ("on".equals(action) || "off".equals(action)) {
            if (!sender.hasPermission(PERMISSION_ADMIN)) {
                settings.sendNoPermission(sender);
                return true;
            }
            handleToggle(sender, args, "on".equals(action));
            return true;
        }

        if (!hasUsePermission(sender)) {
            settings.sendNoPermission(sender);
            return true;
        }
        settings.sendUsage(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission(PERMISSION_ADMIN)) {
            return completions;
        }

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], ACTIONS, completions);
        } else if (args.length == 2) {
            String action = args[0].toLowerCase(Locale.ROOT);
            if (ACTIONS.contains(action)) {
                List<String> names = new ArrayList<>();
                names.add("all");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    names.add(player.getName());
                }
                StringUtil.copyPartialMatches(args[1], names, completions);
            }
        }

        completions.sort(String.CASE_INSENSITIVE_ORDER);
        return completions;
    }

    private void handleToggle(CommandSender sender, String[] args, boolean enable) {
        if (args.length != 2) {
            if (enable) {
                settings.sendUsageOn(sender);
            } else {
                settings.sendUsageOff(sender);
            }
            return;
        }

        String target = args[1];
        if ("all".equalsIgnoreCase(target)) {
            if (enable) {
                nightVisionManager.enableAll();
                settings.sendGlobalEnabled(sender);
            } else {
                nightVisionManager.disableAll();
                settings.sendGlobalDisabled(sender);
            }
            return;
        }

        Player player = Bukkit.getPlayerExact(target);
        if (player == null) {
            if (isKnownOfflinePlayer(target)) {
                settings.sendPlayerOffline(sender, target);
            } else {
                settings.sendPlayerNotFound(sender, target);
            }
            return;
        }

        if (enable) {
            nightVisionManager.enable(player);
            settings.sendEnabled(sender, player.getName());
        } else {
            nightVisionManager.disable(player);
            settings.sendDisabled(sender, player.getName());
        }
    }

    private boolean hasUsePermission(CommandSender sender) {
        return sender.hasPermission(PERMISSION_USE) || sender.hasPermission(PERMISSION_ADMIN);
    }

    private static boolean isKnownOfflinePlayer(String name) {
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        if (cached != null && cached.getName() != null && cached.getName().equalsIgnoreCase(name)) {
            return cached.hasPlayedBefore() || cached.isOnline();
        }

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            String offlineName = offlinePlayer.getName();
            if (offlineName != null && offlineName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
