package org.biotech.api.event.handle;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.biotech.Biotech;
import org.biotech.api.BiotechAPI;
import org.biotech.api.system.gene.GeneContext;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.api.system.trait.core.IStackTraitAccess;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Biotech.MODID)
public class PlayerTraitHandle {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            List<ITrait> traits = getPlayerTraits(player);
            traits.forEach(trait -> {
                trait.tick(new GeneContext(player));
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            List<ITrait> traits = getPlayerTraits(player);
            traits.forEach(trait -> {
                trait.attack(new GeneContext(player),event.getTarget());
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event){
        if (event.getEntity() instanceof ServerPlayer player) {
            List<ITrait> traits = getPlayerTraits(player);
            traits.forEach(trait -> {
                trait.jump(new GeneContext(player));
            });
        }
    }

    @SubscribeEvent
    public static void onCurioAttributeModifier(CurioAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        IStackTraitAccess.getTraits(stack).forEach(trait -> {
            if (trait != null) {
                trait.modifyAttributes(event);
            }
        });
    }





    /**
     * 获取玩家基因相关槽位（gene/xene）中物品携带的词条。
     */
    private static List<ITrait> getPlayerTraits(ServerPlayer player) {
        List<ITrait> traits = new ArrayList<>();
        collectTraitsFromSlots(BiotechAPI.getGeneEquipSlots(player), traits);
        collectTraitsFromSlots(BiotechAPI.getXeneEquipSlots(player), traits);
        return traits;
    }

    private static void collectTraitsFromSlots(IDynamicStackHandler stacks, List<ITrait> traits) {
        if (stacks == null) {
            return;
        }

        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            traits.addAll(IStackTraitAccess.getTraits(stack));
        }
    }
}
