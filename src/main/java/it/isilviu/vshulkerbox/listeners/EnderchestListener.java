package it.isilviu.vshulkerbox.listeners;

import it.isilviu.vshulkerbox.utils.config.model.YamlFile;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class EnderchestListener implements Listener {

    final YamlFile config;

    public EnderchestListener(YamlFile config) {
        this.config = config;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

        if (!config.getBoolean("enderchest", false)) return;

        Player player = event.getPlayer();
        player.openInventory(player.getEnderChest());
    } // For real? This is the whole class?
}
