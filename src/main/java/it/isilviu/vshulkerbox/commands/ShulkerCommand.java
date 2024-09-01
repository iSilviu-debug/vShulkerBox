package it.isilviu.vshulkerbox.commands;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import it.isilviu.vshulkerbox.utils.config.Messages;
import it.isilviu.vshulkerbox.utils.config.model.YamlFile;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("vshulker")
public class ShulkerCommand {

    final YamlFile config;

    public ShulkerCommand(YamlFile config) {
        this.config = config;
    }

    @Subcommand("info")
    public void onInfo(BukkitCommandActor actor) {
        actor.audience().sendMessage(Messages.getMessage("""
                <gradient:blue:yellow>Plugin <gradient:red:blue>vShulkerBox <gradient:yellow:green>v<gradient:green:blue>1.2
                <gray>Plugin developed by <aqua>silvio.top"""));
    }

    @Subcommand("reload")
    @CommandPermission("vshulker.admin")
    public void onReload(BukkitCommandActor actor) {
        actor.audience().sendMessage(Messages.getMessage("messages.reload"));
        config.reload();
    }

    @Subcommand("restore")
    @CommandPermission("vshulker.admin")
    public void onRestore(BukkitCommandActor actor) {
        if (actor.isConsole()) return;
        Player player = actor.getAsPlayer();
        assert player != null;

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (!itemStack.getType().name().contains("SHULKER_BOX")) {
            actor.audience().sendMessage(Messages.getMessage("messages.restore.no-item"));
            return;
        }

        // Auto Restore System. (From bugs)
        NBT.modify(itemStack, (nbtCompound) -> {
            nbtCompound.removeKey("vShulkerBox");

            ReadWriteNBT nbt = nbtCompound.getCompound("BlockEntityTag");
            String backup = nbtCompound.getString("BlockEntityTag_vShulkerBox");
            if (nbt == null || backup == null || backup.isEmpty()) {
                actor.audience().sendMessage(Messages.getMessage("messages.restore.failed"));
                return;
            }

            nbtCompound.removeKey("BlockEntityTag_vShulkerBox");
            nbt.mergeCompound(NBT.parseNBT(backup)); // Replace the backup-ed one.

            actor.audience().sendMessage(Messages.getMessage("messages.restore.success"));
        });

        player.getInventory().setItemInMainHand(itemStack);
    }
}
