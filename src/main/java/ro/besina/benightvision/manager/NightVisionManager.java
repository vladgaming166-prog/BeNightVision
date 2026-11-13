package ro.besina.benightvision.manager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import ro.besina.benightvision.BeNightVision;
import ro.besina.benightvision.config.PluginSettings;

public final class NightVisionManager {

    private static final PotionEffect NIGHT_VISION = new PotionEffect(
            PotionEffectType.NIGHT_VISION,
            PotionEffect.INFINITE_DURATION,
            0,
            false,
            false,
            false
    );

    private static final long REFRESH_INTERVAL_TICKS = 20L * 60L;

    private final BeNightVision plugin;
    private final PluginSettings settings;
    private final Set<UUID> individuallyEnabled = ConcurrentHashMap.newKeySet();
    private final Set<UUID> individuallyDisabled = ConcurrentHashMap.newKeySet();
    private final Set<UUID> mutating = ConcurrentHashMap.newKeySet();

    private BukkitTask refreshTask;

    public NightVisionManager(BeNightVision plugin, PluginSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public void start() {
        if (settings.isGlobalNightVision()) {
            applyToAllOnline();
        }
        refreshTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::refreshOnlinePlayers,
                REFRESH_INTERVAL_TICKS,
                REFRESH_INTERVAL_TICKS
        );
    }

    public void shutdown() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        individuallyEnabled.clear();
        individuallyDisabled.clear();
        mutating.clear();
    }

    public boolean isMutating(UUID uniqueId) {
        return mutating.contains(uniqueId);
    }

    public boolean shouldHaveNightVision(Player player) {
        if (player == null) {
            return false;
        }

        UUID uniqueId = player.getUniqueId();
        if (settings.isGlobalNightVision()) {
            return !individuallyDisabled.contains(uniqueId);
        }
        return individuallyEnabled.contains(uniqueId);
    }

    public void handleJoin(Player player) {
        individuallyDisabled.remove(player.getUniqueId());
        if (shouldHaveNightVision(player)) {
            apply(player);
        }
    }

    public void enableAll() {
        settings.setGlobalNightVision(true);
        individuallyEnabled.clear();
        individuallyDisabled.clear();
        applyToAllOnline();
    }

    public void disableAll() {
        settings.setGlobalNightVision(false);
        individuallyEnabled.clear();
        individuallyDisabled.clear();
        for (Player player : snapshotOnlinePlayers()) {
            remove(player);
        }
    }

    public void enable(Player player) {
        UUID uniqueId = player.getUniqueId();
        individuallyDisabled.remove(uniqueId);
        if (!settings.isGlobalNightVision()) {
            individuallyEnabled.add(uniqueId);
        }
        apply(player);
    }

    public void disable(Player player) {
        UUID uniqueId = player.getUniqueId();
        individuallyEnabled.remove(uniqueId);
        if (settings.isGlobalNightVision()) {
            individuallyDisabled.add(uniqueId);
        }
        remove(player);
    }

    public void applyIfNeeded(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        if (shouldHaveNightVision(player)) {
            apply(player);
        }
    }

    public void apply(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        if (hasManagedEffect(player)) {
            return;
        }

        UUID uniqueId = player.getUniqueId();
        mutating.add(uniqueId);
        try {
            player.addPotionEffect(NIGHT_VISION);
        } finally {
            mutating.remove(uniqueId);
        }
    }

    public void remove(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID uniqueId = player.getUniqueId();
        mutating.add(uniqueId);
        try {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        } finally {
            mutating.remove(uniqueId);
        }
    }

    private void refreshOnlinePlayers() {
        for (Player player : snapshotOnlinePlayers()) {
            applyIfNeeded(player);
        }
    }

    private void applyToAllOnline() {
        for (Player player : snapshotOnlinePlayers()) {
            apply(player);
        }
    }

    private static boolean hasManagedEffect(Player player) {
        PotionEffect current = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
        return current != null
                && current.isInfinite()
                && current.getAmplifier() == 0
                && !current.hasParticles()
                && !current.hasIcon();
    }

    private static Set<Player> snapshotOnlinePlayers() {
        return new HashSet<>(Bukkit.getOnlinePlayers());
    }
}
