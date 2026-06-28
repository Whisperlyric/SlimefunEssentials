package me.justahuman.slimefun_essentials.compat.emi;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ExperienceOrb;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * EMI stack representing an Entity.
 * <p>
 * Note: Full 3D entity rendering is not implemented for 26.1.2, which replaced
 * {@code EntityRenderDispatcher.render} with a state-based pipeline
 * ({@code extractEntity} + {@code submit}) that is not accessible from
 * {@link GuiGraphicsExtractor}. The stack metadata (id, name, tooltip) is
 * complete so EMI can still index and display entity ingredients; the icon
 * falls back to the entity name text.
 */
@SuppressWarnings("DuplicatedCode")
public class EntityEmiStack extends EmiStack {
    private final EntityType<?> type;
    private final @Nullable Entity entity;
    private final boolean baby;

    public EntityEmiStack(EntityType<?> type, boolean baby) {
        this.type = type;
        Entity created = null;
        if (Minecraft.getInstance().level != null) {
            created = type.create(Minecraft.getInstance().level, EntitySpawnReason.COMMAND);
        }
        this.entity = created;
        this.baby = baby && created instanceof Mob;
        if (this.baby) {
            ((Mob) created).setBaby(true);
        }
    }

    @Override
    public EmiStack copy() {
        EntityEmiStack stack = new EntityEmiStack(this.type, this.baby);
        stack.setRemainder(getRemainder().copy());
        stack.comparison = this.comparison;
        return stack;
    }

    @Override
    public boolean isEmpty() {
        return entity == null;
    }

    @Override
    public void render(GuiGraphicsExtractor draw, int x, int y, float delta, int flags) {
        if (entity == null) {
            return;
        }
        EmiDrawContext context = EmiDrawContext.wrap(draw);
        // 26.1.2 entity render pipeline is state-based and not reachable from GuiGraphicsExtractor;
        // fall back to the entity's display name as the icon.
        context.drawText(getName(), x, y, 0xFFFFFFFF);
        if (this.amount > 1) {
            EmiRenderHelper.renderAmount(context, x, y, Component.literal(String.valueOf(this.amount)));
        }
    }

    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public Object getKey() {
        return entity;
    }

    @Override
    public Identifier getId() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(this.type);
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of(getName());
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        final List<ClientTooltipComponent> list = new ArrayList<>();
        if (this.entity != null) {
            list.addAll(getTooltipText().stream().map(EmiPort::ordered).map(ClientTooltipComponent::create).toList());
            final String mod = EmiUtil.getModName(BuiltInRegistries.ENTITY_TYPE.getKey(this.entity.getType()).getNamespace());
            list.add(ClientTooltipComponent.create(EmiPort.ordered(EmiPort.literal(mod, ChatFormatting.BLUE, ChatFormatting.ITALIC))));
            if (!getRemainder().isEmpty()) {
                list.add(new RemainderTooltipComponent(this));
            }
        }
        return list;
    }

    @Override
    public Component getName() {
        return entity != null ? entity.getName() : EmiPort.literal("yet another missingno");
    }

    public boolean isLarge() {
        return !this.baby && !(this.entity instanceof ExperienceOrb);
    }

    public static void drawLivingEntity(GuiGraphicsExtractor ctx, int x, int y, float size, float mouseX, float mouseY, LivingEntity entity) {
        // 26.1.2 实体渲染 API 已重构为基于 EntityRenderState 的管线，
        // 不再提供从 GuiGraphicsExtractor 直接调用 EntityRenderDispatcher.render 的途径。
        // 完整 3D 实体渲染需后续适配新的 GuiRenderState / SubmitNodeCollector 系统。
        EmiDrawContext.wrap(ctx).drawText(entity.getDisplayName(), x, y, 0xFFFFFFFF);
    }
}
