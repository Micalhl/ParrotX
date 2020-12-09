package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Data
@Builder
public class InventoryFreeArea implements InventoryElement {

    private final BaseElement base;
    private final Consumer<InventoryClickEvent> onPlace;
    @Getter
    private final Map<Integer, ItemStack> placedMap = new HashMap<>();

    public static InventoryFreeArea get(final PInventory<?> inv, final String name) {
        return (InventoryFreeArea) inv.getElement(name);
    }

    public void place(final InventoryClickEvent event) {
        if (Objects.isNull(onPlace)) {
            return;
        }
        onPlace.accept(event);
    }

    public ItemStack getPlaced(final int slot) {
        return this.placedMap.get(slot);
    }

    @Override
    public ItemStack parseItem(PInventory<?> inv, int slot) {
        return this.placedMap.getOrDefault(slot, this.base.getItem().get());
    }

    @Override
    public void click(PInventory<?> holder, InventoryClickEvent event) {
        final Inventory inv = holder.getInventory();
        base.getPositions().forEach(slot -> {
            final ItemStack item = inv.getItem(slot);
            if (Objects.nonNull(item) && !item.getType().isAir()) {
                this.placedMap.put(slot, inv.getItem(slot));
            }
        });
    }

    @Override
    public @NotNull BaseElement getBase() {
        return this.base;
    }

    @Override
    public boolean isClickable() {
        return true;
    }
}