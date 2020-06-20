package net.lldv.upgradeablefurnaces.components.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Upgrade {

    private final int upgrade;
    private final UpgradeType type;
    private final int smeltingPercent;
    private final int doublePercent;
    private final String costString;
    private final int cost;

}
