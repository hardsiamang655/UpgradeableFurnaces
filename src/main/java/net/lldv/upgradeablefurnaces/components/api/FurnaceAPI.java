package net.lldv.upgradeablefurnaces.components.api;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.utils.Config;
import net.lldv.llamaeconomy.LlamaEconomy;
import net.lldv.upgradeablefurnaces.UpgradeableFurnaces;
import net.lldv.upgradeablefurnaces.components.data.Furnace;
import net.lldv.upgradeablefurnaces.components.data.Upgrade;
import net.lldv.upgradeablefurnaces.components.data.UpgradeType;
import net.lldv.upgradeablefurnaces.components.tools.Language;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class FurnaceAPI {

    public static HashMap<Location, Furnace> cachedFurnaces = new HashMap<>();
    public static LinkedHashMap<Integer, Upgrade> cachedUpgrades = new LinkedHashMap<>();

    private static Config data;

    public static void initAPI(UpgradeableFurnaces server) {
        server.saveResource("/data/furnaces.yml");
        data = new Config(server.getDataFolder() + "/data/furnaces.yml", Config.YAML);
    }

    public static void placeFurnace(Player player, Location location) {
        String code = getRandomIDCode();
        data.set("Furnaces." + code + ".X", (int) location.x);
        data.set("Furnaces." + code + ".Y", (int) location.y);
        data.set("Furnaces." + code + ".Z", (int) location.z);
        data.set("Furnaces." + code + ".Level", location.level.getName());
        data.set("Furnaces." + code + ".Upgrade", 1);
        data.set("Furnaces." + code + ".Owner", player.getName());
        data.save();
        data.reload();
        cachedFurnaces.put(location, new Furnace(player.getName(), 1, (int) location.x, (int) location.y, (int) location.z, location.level));
        player.sendMessage(Language.getAndReplace("furnace-placed", 1));
        playSound(player, Sound.NOTE_XYLOPHONE);
    }

    public static void placeFurnace(Player player, Location location, int level) {
        if (cachedUpgrades.get(level) == null) {
            placeFurnace(player, location);
            return;
        }
        String code = getRandomIDCode();
        data.set("Furnaces." + code + ".X", (int) location.x);
        data.set("Furnaces." + code + ".Y", (int) location.y);
        data.set("Furnaces." + code + ".Z", (int) location.z);
        data.set("Furnaces." + code + ".Level", location.level.getName());
        data.set("Furnaces." + code + ".Upgrade", level);
        data.set("Furnaces." + code + ".Owner", player.getName());
        data.save();
        data.reload();
        cachedFurnaces.put(location, new Furnace(player.getName(), level, (int) location.x, (int) location.y, (int) location.z, location.level));
        player.sendMessage(Language.getAndReplace("furnace-placed", level));
        playSound(player, Sound.NOTE_XYLOPHONE);
    }

    public static void upgradeFurnace(Player player, Furnace furnace, Upgrade upgrade) {
        if (upgrade.getType() == UpgradeType.MONEY) {
            CompletableFuture.runAsync(() -> {
                if (LlamaEconomy.getAPI().getMoney(player.getName()) >= upgrade.getCost()) {
                    LlamaEconomy.getAPI().reduceMoney(player.getName(), upgrade.getCost());
                    furnace.setUpgrade(furnace.getUpgrade() + 1);
                    setUpgrade(furnace);
                    player.sendMessage(Language.getAndReplace("furnace-upgraded", furnace.getUpgrade()));
                    playSound(player, Sound.RANDOM_LEVELUP);
                } else {
                    player.sendMessage(Language.getAndReplace("need-more-money"));
                    playSound(player, Sound.NOTE_BASS);
                }
            });
        } else if (upgrade.getType() == UpgradeType.EXPERIENCE) {
            if (player.getExperienceLevel() >= upgrade.getCost()) {
                player.setExperience(0, player.getExperienceLevel() - upgrade.getCost());
                furnace.setUpgrade(furnace.getUpgrade() + 1);
                setUpgrade(furnace);
                player.sendMessage(Language.getAndReplace("furnace-upgraded", furnace.getUpgrade()));
                playSound(player, Sound.RANDOM_LEVELUP);
            } else {
                player.sendMessage(Language.getAndReplace("need-more-xp"));
                playSound(player, Sound.NOTE_BASS);
            }
        }
    }

    private static void setUpgrade(Furnace furnace) {
        String id = null;
        for (String s : data.getSection("Furnaces").getAll().getKeys(false)) {
            if (data.getInt("Furnaces." + s + ".X") == furnace.getX() && data.getInt("Furnaces." + s + ".Y") == furnace.getY() && data.getInt("Furnaces." + s + ".Z") == furnace.getZ() && data.getString("Furnaces." + s + ".Level").equals(furnace.getLevel().getName())) {
                id = s;
            }
        }
        data.set("Furnaces." + id + ".Upgrade", furnace.getUpgrade());
        data.save();
        data.reload();
    }

    public static void deleteFurnace(int x, int y, int z, String level) {
        String id = null;
        for (String s : data.getSection("Furnaces").getAll().getKeys(false)) {
            if (data.getInt("Furnaces." + s + ".X") == x && data.getInt("Furnaces." + s + ".Y") == y && data.getInt("Furnaces." + s + ".Z") == z && data.getString("Furnaces." + s + ".Level").equals(level)) {
                id = s;
            }
        }
        Map<String, Object> map = data.getSection("Furnaces").getAllMap();
        map.remove(id);
        data.set("Ban", map);
        data.save();
        data.reload();
        cachedFurnaces.remove(new Location(x, y, z, Server.getInstance().getLevelByName(level)));
    }

    public static void loadUpgrades(Config config) {
        for (String s : config.getSection("Upgrades").getAll().getKeys(false)) {
            UpgradeType type = UpgradeType.valueOf(config.getString("Upgrades." + s + ".CostType"));
            switch (type) {
                case MONEY: {
                    Upgrade upgrade = new Upgrade(Integer.parseInt(s), UpgradeType.MONEY, config.getInt("Upgrades." + s + ".SmeltingSpeedPercent"), config.getInt("Upgrades." + s + ".DoubleResultPercent"), config.getString("Upgrades." + s + ".CostString"), config.getInt("Upgrades." + s + ".Cost"));
                    cachedUpgrades.put(Integer.parseInt(s), upgrade);
                }
                break;
                case EXPERIENCE: {
                    Upgrade upgrade = new Upgrade(Integer.parseInt(s), UpgradeType.EXPERIENCE, config.getInt("Upgrades." + s + ".SmeltingSpeedPercent"), config.getInt("Upgrades." + s + ".DoubleResultPercent"), config.getString("Upgrades." + s + ".CostString"), config.getInt("Upgrades." + s + ".Cost"));
                    cachedUpgrades.put(Integer.parseInt(s), upgrade);
                }
                break;
            }
        }
    }

    public static void loadFurnances() {
        for (String s : data.getSection("Furnaces").getAll().getKeys(false)) {
            String owner = data.getString("Furnaces." + s + ".Owner");
            int upgrade = data.getInt("Furnaces." + s + ".Upgrade");
            int x = data.getInt("Furnaces." + s + ".X");
            int y = data.getInt("Furnaces." + s + ".Y");
            int z = data.getInt("Furnaces." + s + ".Z");
            String levelString = data.getString("Furnaces." + s + ".Level");
            Level level = Server.getInstance().getLevelByName(levelString);
            Furnace furnace = new Furnace(owner, upgrade, x, y, z, level);
            cachedFurnaces.put(new Location(x, y, z, level), furnace);
        }
    }

    public static String getRandomIDCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        Random rnd = new Random();
        while (stringBuilder.length() < 10) {
            int index = (int) (rnd.nextFloat() * chars.length());
            stringBuilder.append(chars.charAt(index));
        }
        return stringBuilder.toString();
    }

    public static void playSound(Player player, Sound sound) {
        PlaySoundPacket packet = new PlaySoundPacket();
        packet.name = sound.getSound();
        packet.x = new Double(player.getLocation().getX()).intValue();
        packet.y = (new Double(player.getLocation().getY())).intValue();
        packet.z = (new Double(player.getLocation().getZ())).intValue();
        packet.volume = 1.0F;
        packet.pitch = 1.0F;
        player.dataPacket(packet);
    }

}
