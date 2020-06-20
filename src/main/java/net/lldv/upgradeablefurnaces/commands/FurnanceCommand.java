package net.lldv.upgradeablefurnaces.commands;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.tag.CompoundTag;
import net.lldv.upgradeablefurnaces.components.api.FurnaceAPI;
import net.lldv.upgradeablefurnaces.components.tools.Language;

public class FurnanceCommand extends Command {

    public FurnanceCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("furnace.command")) {
                if (args.length == 3) {
                    if (args[0].equalsIgnoreCase("give")) {
                        Player player = Server.getInstance().getPlayer(args[1]);
                        if (player != null) {
                            try {
                                int i = Integer.parseInt(args[2]);
                                if (FurnaceAPI.cachedUpgrades.get(i) != null) {
                                    Item item = Item.get(61);
                                    item.setCount(1);
                                    item.setNamedTag(new CompoundTag().putInt("furnace-level", i));
                                    item.setLore(Language.getAndReplaceNP("furnace-item", i));
                                    player.getInventory().addItem(item);
                                    player.sendMessage(Language.getAndReplace("furnace-received", i));
                                    FurnaceAPI.playSound(player, Sound.RANDOM_LEVELUP);
                                } else sender.sendMessage(Language.getAndReplace("invalid-level"));
                            } catch (NumberFormatException e) {
                                sender.sendMessage(Language.getAndReplace("invalid-level"));
                            }
                        } else sender.sendMessage(Language.getAndReplace("player-not-online", args[1]));
                    } else sender.sendMessage(Language.getAndReplace("furnace-usage", getName()));
                } else sender.sendMessage(Language.getAndReplace("furnace-usage", getName()));
            } else sender.sendMessage(Language.getAndReplace("no-permission"));
        }
        return false;
    }
}
