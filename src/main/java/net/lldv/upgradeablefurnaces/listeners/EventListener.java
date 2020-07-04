package net.lldv.upgradeablefurnaces.listeners;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.*;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.inventory.FurnaceSmeltEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.tag.CompoundTag;
import net.lldv.upgradeablefurnaces.UpgradeableFurnaces;
import net.lldv.upgradeablefurnaces.components.api.FurnaceAPI;
import net.lldv.upgradeablefurnaces.components.data.Furnace;
import net.lldv.upgradeablefurnaces.components.data.Upgrade;
import net.lldv.upgradeablefurnaces.components.tools.Language;
import net.lldv.upgradeablefurnaces.forms.FormWindows;

import java.util.ArrayList;

public class EventListener implements Listener {

    private final ArrayList<Player> cooldown = new ArrayList<>();

    @EventHandler
    public void on(FurnaceSmeltEvent event) {
        BlockEntityFurnace entityFurnace = event.getFurnace();
        Furnace furnace = FurnaceAPI.cachedFurnaces.get(new Location(entityFurnace.x, entityFurnace.y, entityFurnace.z, entityFurnace.level));
        if (furnace != null) {
            Upgrade upgrade = FurnaceAPI.cachedUpgrades.get(furnace.getUpgrade());
            if (upgrade != null) {
                int add = (200 * upgrade.getSmeltingPercent() / 100) + 200;
                entityFurnace.setCookTime(add);
                double d = Math.random() * 100;
                boolean b = d <= upgrade.getDoublePercent();
                if (event.getResult().count == 64) b = false;
                if (b) event.getResult().setCount(event.getResult().count + 1);
            }
        }
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                if (block.getId() == BlockID.FURNACE || block.getId() == BlockID.BURNING_FURNACE) {
                    Furnace furnace = FurnaceAPI.cachedFurnaces.get(new Location((int) block.x, (int) block.y, (int) block.z, block.level));
                    if (furnace != null) {
                        if (furnace.getOwner().equals(player.getName())) {
                            FormWindows.openFurnaceMenu(player, furnace);
                            event.setCancelled(true);
                        } else {
                            FormWindows.openFurnaceInfo(player, furnace);
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void on(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (item.getId() == BlockID.FURNACE) {
            if (cooldown.contains(player)) {
                player.sendMessage(Language.getAndReplace("cooldown"));
                FurnaceAPI.playSound(player, Sound.NOTE_BASS);
                event.setCancelled(true);
                return;
            }
            if (item.getNamedTag() == null) {
                FurnaceAPI.placeFurnace(player, event.getBlock().getLocation());
                cooldown.add(player);
                Server.getInstance().getScheduler().scheduleDelayedTask(UpgradeableFurnaces.getInstance(), () -> cooldown.remove(player), 40);
            } else {
                FurnaceAPI.placeFurnace(player, event.getBlock().getLocation(), item.getNamedTag().getInt("furnace-level"));
                cooldown.add(player);
                Server.getInstance().getScheduler().scheduleDelayedTask(UpgradeableFurnaces.getInstance(), () -> cooldown.remove(player), 40);
            }

        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void on(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Furnace furnace = FurnaceAPI.cachedFurnaces.get(new Location((int) block.x, (int) block.y, (int) block.z, block.level));
        if (furnace != null) {
            if (player.getGamemode() == 1) {
                if (player.isSneaking()) {
                    Item item = Item.get(61);
                    item.setNamedTag(new CompoundTag().putInt("furnace-level", furnace.getUpgrade()));
                    item.setLore(Language.getAndReplaceNP("furnace-item", furnace.getUpgrade()));
                    FurnaceAPI.deleteFurnace((int) block.x, (int) block.y, (int) block.z, block.level.getName());
                    player.sendMessage(Language.getAndReplace("furnace-removed-gamemode", furnace.getUpgrade()));
                    player.getInventory().addItem(item);
                    FurnaceAPI.playSound(player, Sound.NOTE_BASS);
                } else {
                    player.sendMessage(Language.getAndReplace("remove-sneak"));
                    event.setCancelled(true);
                }
            } else {
                for (Item drop : event.getDrops()) {
                    if (drop.getId() == BlockID.FURNACE || drop.getId() == BlockID.BURNING_FURNACE) {
                        drop.setNamedTag(new CompoundTag().putInt("furnace-level", furnace.getUpgrade()));
                        drop.setLore(Language.getAndReplaceNP("furnace-item", furnace.getUpgrade()));
                        FurnaceAPI.deleteFurnace((int) block.x, (int) block.y, (int) block.z, block.level.getName());
                        player.sendMessage(Language.getAndReplace("furnace-removed", furnace.getUpgrade()));
                        FurnaceAPI.playSound(player, Sound.NOTE_BASS);
                    }
                }
            }
        }
    }
}
