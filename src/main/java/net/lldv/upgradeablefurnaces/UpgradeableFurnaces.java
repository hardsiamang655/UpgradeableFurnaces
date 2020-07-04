package net.lldv.upgradeablefurnaces;

import cn.nukkit.plugin.PluginBase;
import net.lldv.upgradeablefurnaces.commands.FurnaceCommand;
import net.lldv.upgradeablefurnaces.components.api.FurnaceAPI;
import net.lldv.upgradeablefurnaces.components.tools.Language;
import net.lldv.upgradeablefurnaces.forms.FormListener;
import net.lldv.upgradeablefurnaces.listeners.EventListener;

public class UpgradeableFurnaces extends PluginBase {

    private static UpgradeableFurnaces instance;

    @Override
    public void onEnable() {
        instance = this;
        try {
            saveDefaultConfig();
            FurnaceAPI.initAPI(this);
            Language.initConfiguration();
            FurnaceAPI.loadUpgrades(getConfig());
            FurnaceAPI.loadFurnances();
            getServer().getPluginManager().registerEvents(new EventListener(), this);
            getServer().getPluginManager().registerEvents(new FormListener(), this);
            getServer().getCommandMap().register(getConfig().getString("Commands.Furnace"), new FurnaceCommand(getConfig().getString("Commands.Furnace")));
            getLogger().info("§aUpgradeableFurnaces successfully started.");
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().error("§4Failed to load UpgradeableFurnaces.");
        }
    }

    public static UpgradeableFurnaces getInstance() {
        return instance;
    }
}
