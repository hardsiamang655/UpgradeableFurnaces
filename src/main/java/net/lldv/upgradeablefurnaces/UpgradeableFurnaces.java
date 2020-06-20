package net.lldv.upgradeablefurnaces;

import cn.nukkit.plugin.PluginBase;
import net.lldv.upgradeablefurnaces.commands.FurnanceCommand;
import net.lldv.upgradeablefurnaces.components.api.FurnaceAPI;
import net.lldv.upgradeablefurnaces.components.tools.Language;
import net.lldv.upgradeablefurnaces.forms.FormListener;
import net.lldv.upgradeablefurnaces.listeners.EventListener;

public class UpgradeableFurnaces extends PluginBase {

    private static UpgradeableFurnaces instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        FurnaceAPI.initAPI(this);
        Language.initConfiguration();
        FurnaceAPI.loadUpgrades(getConfig());
        FurnaceAPI.loadFurnances();
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getServer().getPluginManager().registerEvents(new FormListener(), this);
        getServer().getCommandMap().register(getConfig().getString("Commands.Furnace"), new FurnanceCommand(getConfig().getString("Commands.Furnace")));
    }

    public static UpgradeableFurnaces getInstance() {
        return instance;
    }
}
