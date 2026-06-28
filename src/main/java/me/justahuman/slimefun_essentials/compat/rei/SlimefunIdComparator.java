package me.justahuman.slimefun_essentials.compat.rei;

import me.justahuman.slimefun_essentials.client.SlimefunItemStack;
import me.justahuman.slimefun_essentials.client.SlimefunRegistry;
import me.justahuman.slimefun_essentials.utils.Utils;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import net.minecraft.world.item.ItemStack;

public class SlimefunIdComparator implements EntryComparator<ItemStack> {
    @Override
    public long hash(ComparisonContext context, ItemStack stack) {
        final String sfId = Utils.getSlimefunId(stack);
        if (sfId != null) {
            final SlimefunItemStack slimefunItemStack = SlimefunRegistry.getSlimefunItem(sfId);
            final String addon = slimefunItemStack != null ? slimefunItemStack.addon() : "slimefun";
            return (addon + ":" + sfId).hashCode();
        }
        return 123;
    }
}
