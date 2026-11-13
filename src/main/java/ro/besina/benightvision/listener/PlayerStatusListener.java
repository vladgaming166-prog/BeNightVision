package ro.besina.benightvision.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import ro.besina.benightvision.BeNightVision;
import ro.besina.benightvision.manager.NightVisionManager;

public final class PlayerStatusListener implements Listener {

    private final BeNightVision plugin;
    private final NightVisionManager nightVisionManager;

    public PlayerStatusListener(BeNightVision plugin, NightVisionManager nightVisionManager) {
        this.plugin = plugin;
        this.nightVisionManager = nightVisionManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        scheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                nightVisionManager.handleJoin(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        scheduler().runTaskLater(plugin, () -> nightVisionManager.applyIfNeeded(player), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        nightVisionManager.applyIfNeeded(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!PotionEffectType.NIGHT_VISION.equals(event.getModifiedType())) {
            return;
        }
        if (nightVisionManager.isMutating(player.getUniqueId())) {
            return;
        }

        EntityPotionEffectEvent.Action action = event.getAction();
        if (action != EntityPotionEffectEvent.Action.REMOVED
                && action != EntityPotionEffectEvent.Action.CLEARED) {
            return;
        }
        if (!nightVisionManager.shouldHaveNightVision(player)) {
            return;
        }

        scheduler().runTaskLater(plugin, () -> nightVisionManager.applyIfNeeded(player), 1L);
    }

    private BukkitScheduler scheduler() {
        return plugin.getServer().getScheduler();
    }
}
