package me.justahuman.slimefun_essentials.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import me.justahuman.slimefun_essentials.api.def.DrawMode;
import me.justahuman.slimefun_essentials.client.RecipeCategory;
import me.justahuman.slimefun_essentials.client.SlimefunRecipe;
import me.justahuman.slimefun_essentials.client.component.RecipeDisplayComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SlimefunEmiRecipe implements EmiRecipe {
    private final RecipeCategory category;
    private final SlimefunRecipe recipe;
    private final SlimefunEmiCategory emiCategory;
    private final List<EmiIngredient> inputs = new ArrayList<>();
    private final List<EmiStack> outputs = new ArrayList<>();

    public SlimefunEmiRecipe(RecipeCategory category, SlimefunRecipe recipe, SlimefunEmiCategory emiCategory) {
        this.category = category;
        this.recipe = recipe;
        this.emiCategory = emiCategory;
        this.inputs.addAll(EmiIntegration.RECIPE_INTERPRETER.getInputIngredients(recipe));
        this.outputs.addAll(EmiIntegration.RECIPE_INTERPRETER.getOutputStacks(recipe));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return this.emiCategory;
    }

    @Override
    public @Nullable Identifier getId() {
        return null;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return this.inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return this.outputs;
    }

    @Override
    public int getDisplayWidth() {
        return category.display().width(this.recipe);
    }

    @Override
    public int getDisplayHeight() {
        return category.display().height(this.recipe);
    }

    @Override
    public void addWidgets(dev.emi.emi.api.widget.WidgetHolder widgets) {
        for (RecipeDisplayComponent component : category.display().components(recipe)) {
            int x = component.x();
            int y = component.y();
            int width = component.width();
            int height = component.height();
            List<ClientTooltipComponent> tooltip = component.tooltip(DrawMode.LIGHT, recipe);
            widgets.addDrawable(x, y, width, height, (draw, mouseX, mouseY, delta) ->
                    component.draw(recipe, DrawMode.LIGHT, draw, 0, 0)).tooltip((mx, my) -> tooltip);

            if (component.type().equals("slot") || component.type().equals("large_slot")) {
                int index = component.index();
                boolean large = component.type().equals("large_slot");
                if (index <= -1) {
                    widgets.addSlot(EmiStack.of(recipe.parent().itemStack()), x, y).large(large).drawBack(false);
                } else if (!component.output() && index > 0 && index <= inputs.size()) {
                    widgets.addSlot(inputs.get(--index), x, y).large(large).drawBack(false);
                } else if (component.output() && index > 0 && index <= outputs.size()) {
                    widgets.addSlot(outputs.get(--index), x, y).recipeContext(this).large(large).drawBack(false);
                }
            }
        }
    }
}
