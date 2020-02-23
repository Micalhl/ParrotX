package org.serverct.parrot.parrotx.data;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.flags.Timestamp;
import org.serverct.parrot.parrotx.flags.Uniqued;
import org.serverct.parrot.parrotx.utils.LocaleUtil;

import java.util.HashMap;
import java.util.Map;

public class Goal implements Timestamp, Uniqued {

    private PPlugin plugin;
    private PID id;
    private long startTime;
    @Getter
    private Map<Type, Integer> digitalRemain = new HashMap<>();
    @Getter
    private Map<Material, Integer> itemRemain = new HashMap<>();

    public Goal(PID id, Map<Type, Integer> digital, Map<Material, Integer> item) {
        this.id = id;
        this.plugin = id.getPlugin();
        this.digitalRemain = digital;
        this.itemRemain = item;
        this.startTime = System.currentTimeMillis();
    }

    /*
     * Goal:
     *   StartTime: 1582454325042
     *   Remain:
     *     Digital:
     *       Money: 111
     *       Experience: 999
     *     Items:
     *       WOOL: 999
     *       WOOD: 666
     */
    public Goal(PID id, @NonNull ConfigurationSection section) {
        this.id = id;
        this.plugin = id.getPlugin();
        this.startTime = section.getLong("StartTime");
        ConfigurationSection remain = section.getConfigurationSection("Remain");
        if (remain != null) {
            try {
                ConfigurationSection digital = remain.getConfigurationSection("Digital");
                if (digital != null) {
                    for (String type : digital.getKeys(false)) {
                        digitalRemain.put(Type.valueOf(type.toUpperCase()), digital.getInt(type));
                    }
                }
                ConfigurationSection item = remain.getConfigurationSection("Items");
                if (item != null) {
                    for (String material : item.getKeys(false)) {
                        itemRemain.put(Material.valueOf(material.toUpperCase()), item.getInt(material));
                    }
                }
            } catch (Throwable e) {
                plugin.lang.logError(LocaleUtil.LOAD, "目标(" + id.getKey() + ")", e.toString());
            }
        }
    }

    public void save(@NonNull ConfigurationSection section) {
        section.set("StartTime", startTime);
        ConfigurationSection remain = section.createSection("Remain");
        ConfigurationSection digital = remain.createSection("Digital");
        for (Type type : digitalRemain.keySet()) {
            digital.set(type.toString(), digitalRemain.get(type));
        }
        ConfigurationSection item = remain.createSection("Items");
        for (Material material : itemRemain.keySet()) {
            item.set(material.toString(), itemRemain.get(material));
        }
    }

    public int contribute(Type type, int amount) {
        if (type == Type.ITEM) {
            plugin.lang.logError(LocaleUtil.LOAD, "目标(" + id.getKey() + ")", "尝试数字化提交物品.");
            return 0;
        }
        int result = digitalRemain.get(type) - amount;
        if (result <= 0) {
            digitalRemain.remove(type);
            return result * -1;
        }
        digitalRemain.put(type, result);
        return 0;
    }

    public Map<Material, Integer> contribute(Inventory inventory) {
        Map<Material, Integer> result = new HashMap<>();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                Material material = item.getType();
                if (itemRemain.containsKey(material)) {
                    inventory.removeItem(item);
                    int resultAmount = itemRemain.get(material) - item.getAmount();
                    result.put(material, item.getAmount());
                    if (resultAmount < 0) {
                        item.setAmount(resultAmount * -1);
                        inventory.addItem(item);
                    }
                    if (resultAmount <= 0) {
                        itemRemain.remove(material);
                    }
                    if (resultAmount > 0) {
                        itemRemain.put(material, resultAmount);
                    }
                }
            }
        }
        return result;
    }

    public boolean isFinish() {
        return digitalRemain.isEmpty() && itemRemain.isEmpty();
    }

    @Override
    public long getTimestamp() {
        return startTime;
    }

    @Override
    public void setTime(long time) {
        this.startTime = time;
    }

    @Override
    public PID getID() {
        return id;
    }

    @Override
    public void setID(@NonNull PID pid) {
        this.id = pid;
    }

    public enum Type {
        ITEM, MONEY, EXPERIENCE, POINT;
    }
}
