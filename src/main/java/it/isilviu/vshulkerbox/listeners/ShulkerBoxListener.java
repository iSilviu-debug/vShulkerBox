package it.isilviu.vshulkerbox.listeners;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import io.papermc.paper.event.player.AsyncChatEvent;
import it.isilviu.vshulkerbox.utils.config.Messages;
import it.isilviu.vshulkerbox.utils.config.model.YamlFile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ShulkerBoxListener implements Listener {

    final HashMap<UUID, AbstractMap.SimpleEntry<Integer, Inventory>> shulkerBoxes = new HashMap<>();

    public HashMap<UUID, AbstractMap.SimpleEntry<Integer, Inventory>> shulkerBoxes() {
        return shulkerBoxes;
    }

    public ShulkerBoxListener(YamlFile config) {
        this.config = config;
    }

    final YamlFile config;

    // The idea is:
    // 1. Get Items in the Shulker and clone it.
    // 2. Remove the content of the Shulker.
    // |- Set a backup for the shulker. (Admin command)
    // 3. Identify the Shulker, so we can prevent the moving of the Shulker.
    // 4. Open a GUI with the cloned items.
    // 5. When the GUI is closed, put the items back in the Shulker.

    // When you have the shulker open, and you die, we close the inventory before he dies. (PlayerDamageEvent, finaldamage <= 0)

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;

        if (config.getBoolean("shift-click", true) && !event.getPlayer().isSneaking()) return;

        Player player = event.getPlayer();

        ItemStack item = event.getItem();
        if (item == null || !item.getType().name().contains("SHULKER_BOX")) return;

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

        player.closeInventory(); // Close the inventory, if you have one open. (Prevent dupe)

        int slot = event.getHand() == EquipmentSlot.HAND ? player.getInventory().getHeldItemSlot() : 40;
        this.openInventory(player, item, slot);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(InventoryClickEvent event) {
        if (event.isCancelled()) return; // Imagine if someone open the Auction, and steal all the items. Lucky, we have this line. **
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getAction() != InventoryAction.PICKUP_HALF) return; // Right click action.

        if (!config.getBoolean("anywhere", true)) return;

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null && clickedInventory.getType() != InventoryType.PLAYER) return; // ** And this also.
        // TODO: Next update, i will add a thing to view shulker in /invsee

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.getType().name().contains("SHULKER_BOX")) return;

        event.setResult(Event.Result.DENY);

        // Always possible if you close inventory :D
        player.updateInventory();
        player.closeInventory();

        this.openInventory(player, item, event.getSlot());
    }

    public void openInventory(Player player, ItemStack item, int slot) {

        if (!(item.getItemMeta() instanceof BlockStateMeta meta)) return;
        // #getBlockState is a meanness.
        if (!(meta.getBlockState() instanceof org.bukkit.block.ShulkerBox shulkerBox)) return;
        ItemStack[] itemStacks = shulkerBox.getInventory().getContents(); // I think, we don't need to clone.

        ReadWriteNBT checkNbt = NBT.itemStackToNBT(item).getCompound("tag");
        if (checkNbt != null) {
            UUID uuid = checkNbt.getUUID("vShulkerBox");
            if (uuid != null) {
                if (!shulkerBoxes.containsKey(uuid)) {
                    if (!config.getBoolean("auto-restore")) {
                        // Send a message to the player. THIS IS A BUG! (SERVER STOPPED?)
                        player.sendMessage(Messages.getMessage("messages.no-restore"));
                        return;
                    }

                    // Auto Restore System. (From bugs)
                    NBT.modify(item, (nbtCompound) -> {
                        nbtCompound.removeKey("vShulkerBox");

                        ReadWriteNBT nbt = nbtCompound.getCompound("BlockEntityTag");
                        String backup = nbtCompound.getString("BlockEntityTag_vShulkerBox");
                        if (nbt == null || backup == null || backup.isEmpty()) return;

                        nbtCompound.removeKey("BlockEntityTag_vShulkerBox");
                        nbt.mergeCompound(NBT.parseNBT(backup)); // Replace the backup-ed one.
                    });

                    this.openInventory(player, item, slot); // Recursive call.
                }

                player.openInventory(shulkerBoxes.get(uuid).getValue()); // SYNCRONIZED
                return;
            }
        }

        NBT.modify(item, (nbtCompound) -> {
            // get BlockEntityTag
            // rename Items -> Items_vShulkerBox
            ReadWriteNBT nbt = nbtCompound.getCompound("BlockEntityTag");
            if (nbt != null) {
                nbt.removeKey("Items");
                nbtCompound.setString("BlockEntityTag_vShulkerBox", nbt.toString());
            }
        });

        NBT.modify(item, (nbtCompound) -> {
            nbtCompound.setUUID("vShulkerBox", player.getUniqueId());
        });

        player.getInventory().setItem(slot, item); // If you delete this line, you can dupe. Anyway, is here haha.

        Component title = meta.displayName();
        Inventory inventory = Bukkit.createInventory(null, InventoryType.SHULKER_BOX, title == null ? Component.empty() : title);
        inventory.setContents(itemStacks);


        player.openInventory(inventory);

        shulkerBoxes.put(player.getUniqueId(), new AbstractMap.SimpleEntry<>(slot, inventory));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        AbstractMap.SimpleEntry<Integer, Inventory> entry = shulkerBoxes.remove(player.getUniqueId());
        if (entry == null) return;

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
            nbtCompound.removeKey("BlockEntityTag_vShulkerBox");
        });

        player.getInventory().setItem(entry.getKey(), item);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDie(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getFinalDamage() <= 0) return;

        if (!shulkerBoxes.containsKey(player.getUniqueId())) return;
        player.closeInventory();
    }

    /** Secure method to remove the shulker box from the player's inventory */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractInventory(InventoryClickEvent event) {
        List<ItemStack> itemStackList = Lists.newArrayList();

        ItemStack item = event.getCurrentItem(), cursor = event.getCursor();
        if (item != null) itemStackList.add(item);
        itemStackList.add(cursor); // Not Null.

        if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
            ItemStack itemHotbar = event.getHotbarButton() == -1
                    ? event.getWhoClicked().getInventory().getItemInOffHand()
                    : event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
            if (itemHotbar != null) itemStackList.add(itemHotbar);
        }

        for (ItemStack itemStack : itemStackList) {
            ReadWriteNBT nbt = NBT.itemStackToNBT(itemStack).getCompound("tag");
            if (nbt != null) {
                UUID uuid = nbt.getUUID("vShulkerBox");
                if (uuid == null) continue;
                if (!shulkerBoxes.containsKey(uuid)) continue;

                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
            }
        }
    }
}
