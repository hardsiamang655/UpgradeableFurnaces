package net.lldv.upgradeablefurnaces.components.data;

import cn.nukkit.level.Level;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Furnace {

    private final String owner;
    private int upgrade;
    private int x;
    private int y;
    private int z;
    private Level level;

}
