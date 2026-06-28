package me.justahuman.slimefun_essentials.compat.jei;

import me.justahuman.slimefun_essentials.client.SlimefunItemStack;
import me.justahuman.slimefun_essentials.client.SlimefunRegistry;
import me.justahuman.slimefun_essentials.utils.Utils;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlimefunIdInterpreter implements ISubtypeInterpreter<@NotNull ItemStack> {
    private final ISubtypeInterpreter<@NotNull ItemStack> defaultInterpreter;

    public SlimefunIdInterpreter(ISubtypeInterpreter<@NotNull ItemStack> defaultInterpreter) {
        this.defaultInterpreter = defaultInterpreter;
    }

    @Override
    public @Nullable Object getSubtypeData(@NotNull ItemStack ingredient, @NotNull UidContext context) {
        final String sfId = Utils.getSlimefunId(ingredient);
        if (sfId == null) {
            return this.defaultInterpreter == null ? null : this.defaultInterpreter.getSubtypeData(ingredient, context);
        }
        final SlimefunItemStack slimefunItemStack = SlimefunRegistry.getSlimefunItem(sfId);
        if (slimefunItemStack != null) {
            return slimefunItemStack.addon() + ":" + sfId;
        }
        return "slimefun:" + sfId;
    }
}
