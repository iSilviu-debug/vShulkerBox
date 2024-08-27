package it.isilviu.vshulkerbox;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import it.isilviu.vshulkerbox.commands.ShulkerCommand;
import it.isilviu.vshulkerbox.listeners.EnderchestListener;
import it.isilviu.vshulkerbox.listeners.ShulkerBoxListener;
import it.isilviu.vshulkerbox.utils.config.model.YamlFile;
import it.isilviu.vshulkerbox.utils.bstats.bukkit.Metrics;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;

public class vShulkerBox extends JavaPlugin {

    static vShulkerBox INSTANCE;
    public static vShulkerBox instance() {
        return INSTANCE;
    }

    private YamlFile config;
    private ShulkerBoxListener shulkerBoxListener; // Really? A field for a listener?

    @Override
    public void onEnable() {
        INSTANCE = this;

        // METRICS
        new Metrics(this, 23199);

        // From Item-NBT-API wiki
        if (!NBT.preloadApi()) {
            getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Configuration
        this.config = new YamlFile(this, "config.yml");

        // Register the event listener
        this.shulkerBoxListener = new ShulkerBoxListener(config);
        registerListeners(shulkerBoxListener, new EnderchestListener(config));

        // Commands
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.register(new ShulkerCommand(config));
        // (Optional) Register colorful tooltips (Works on 1.13+ only) // From the wiki.
        handler.registerBrigadier();

        getLogger().info("vShulkerBox has been enabled!");
    }

    @Override
    public void onDisable() {
        shulkerBoxListener.shulkerBoxes().forEach((uuid, entry) -> { // Silly, but it's a free source. (I tried .closeInventory anyway)
            Player player = getServer().getPlayer(uuid);
            if (player == null) return;

            ItemStack item = player.getInventory().getItem(entry.getKey());
            Inventory inventory = entry.getValue();

            // Close even for the viewers. (NO DUPE).
            inventory.getViewers().stream().filter(humanEntity -> !humanEntity.getUniqueId() // Need the filter? IDK, too lazy to test. IS A FREE SOURCE? OR NOT?
                            .equals(player.getUniqueId()))
                    .forEach(HumanEntity::closeInventory);

            if (item == null) return;
            if (!(item.getItemMeta() instanceof BlockStateMeta meta)) return;
            if (!(meta.getBlockState() instanceof ShulkerBox shulkerBox)) return;

            shulkerBox.getInventory().setContents(inventory.getContents());
            meta.setBlockState(shulkerBox);

            item.setItemMeta(meta);

            NBT.modify(item, (nbtCompound) -> {
                nbtCompound.removeKey("vShulkerBox");
                ReadWriteNBT nbt = nbtCompound.getCompound("BlockEntityTag");
                if (nbt != null) nbt.removeKey("Items_vShulkerBox");
            });

            player.getInventory().setItem(entry.getKey(), item);

            player.closeInventory();
        });
    }

    void registerListeners(Listener... listeners) {
        for (Listener listener : listeners)
            getServer().getPluginManager().registerEvents(listener, this);
    }

    public YamlFile config() {
        return config;
    }
}
