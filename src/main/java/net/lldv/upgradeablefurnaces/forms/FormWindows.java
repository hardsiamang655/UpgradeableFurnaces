package net.lldv.upgradeablefurnaces.forms;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import net.lldv.upgradeablefurnaces.components.api.FurnaceAPI;
import net.lldv.upgradeablefurnaces.components.data.Furnace;
import net.lldv.upgradeablefurnaces.components.data.Upgrade;
import net.lldv.upgradeablefurnaces.components.tools.Language;
import net.lldv.upgradeablefurnaces.forms.modal.ModalForm;
import net.lldv.upgradeablefurnaces.forms.simple.SimpleForm;

public class FormWindows {

    public static void openFurnaceMenu(Player player, Furnace furnace) {
        Upgrade upgrade = FurnaceAPI.cachedUpgrades.get(furnace.getUpgrade());
        SimpleForm form = new SimpleForm.Builder(Language.getAndReplaceNP("furnace-menu-title"), Language.getAndReplaceNP("furnace-menu-content", furnace.getUpgrade(), upgrade.getSmeltingPercent(), upgrade.getDoublePercent()))
                .addButton(new ElementButton(Language.getAndReplaceNP("furnace-upgrade-button"),
                        new ElementButtonImageData("url", Language.getAndReplaceNP("furnace-upgrade-image"))), e -> {
                    if (FurnaceAPI.cachedUpgrades.get(furnace.getUpgrade() + 1) == null) {
                        player.sendMessage(Language.getAndReplace("furnace-max-level"));
                        return;
                    }
                    Upgrade nextUpgrade = FurnaceAPI.cachedUpgrades.get(furnace.getUpgrade() + 1);
                    ModalForm modalForm = new ModalForm.Builder(Language.getAndReplaceNP("upgrade-menu-title"), Language.getAndReplaceNP("upgrade-menu-content", nextUpgrade.getUpgrade(), nextUpgrade.getCost(), nextUpgrade.getCostString()),
                            Language.getAndReplaceNP("upgrade-menu-upgrade"), Language.getNoPrefix("upgrade-menu-back"))
                            .onYes(d -> FurnaceAPI.upgradeFurnace(player, furnace, nextUpgrade))
                            .onNo(d -> openFurnaceMenu(player, furnace))
                            .build();
                    modalForm.send(player);
                })
                .build();
        form.send(player);
    }

    public static void openFurnaceInfo(Player player, Furnace furnace) {
        Upgrade upgrade = FurnaceAPI.cachedUpgrades.get(furnace.getUpgrade());
        SimpleForm form = new SimpleForm.Builder(Language.getAndReplaceNP("furnace-menu-title"), Language.getAndReplaceNP("furnace-menu-content", furnace.getUpgrade(), upgrade.getSmeltingPercent(), upgrade.getDoublePercent()))
                .build();
        form.send(player);
    }

}
