package me.justahuman.slimefun_essentials.client;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record SlimefunItemStack(String id, ItemStack itemStack) {
    public void setCustomModelData(int customModelData) {
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(new ArrayList<>(List.of((float) customModelData)), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
    }
    
    public SlimefunItemStack copy() {
        return new SlimefunItemStack(id, itemStack.copy());
    }
}
