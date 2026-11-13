package ro.besina.benightvision;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ro.besina.benightvision.command.NightVisionCommand;
import ro.besina.benightvision.config.PluginSettings;
import ro.besina.benightvision.listener.PlayerStatusListener;
import ro.besina.benightvision.manager.NightVisionManager;

public final class BeNightVision extends JavaPlugin {

    private PluginSettings settings;
    private NightVisionManager nightVisionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        this.settings = new PluginSettings(this);
        this.settings.load();

        this.nightVisionManager = new NightVisionManager(this, settings);
        this.nightVisionManager.start();

        NightVisionCommand command = new NightVisionCommand(settings, nightVisionManager);
        if (!registerCommand("bnv", command) || !registerCommand("benightvision", command)) {
            getLogger().severe("Commands are missing from plugin.yml. Disabling BeNightVision.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerStatusListener(this, nightVisionManager), this);
        getLogger().info("BeNightVision v" + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (nightVisionManager != null) {
            nightVisionManager.shutdown();
        }
        getLogger().info("BeNightVision disabled.");
    }

    private boolean registerCommand(String name, NightVisionCommand executor) {
        PluginCommand pluginCommand = getCommand(name);
        if (pluginCommand == null) {
            return false;
        }

        pluginCommand.setExecutor(executor);
        pluginCommand.setTabCompleter(executor);
        return true;
    }
}
