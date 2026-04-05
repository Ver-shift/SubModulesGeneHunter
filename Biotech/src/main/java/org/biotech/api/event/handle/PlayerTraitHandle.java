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
import org.biotech.api.system.gene.GeneInstance;
import org.biotech.api.init.DataComponentInit;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.component.TraitComp;
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

        // 1) XeneItem 直接挂在 ItemStack 上的词条
        TraitComp xeneComp = stack.get(DataComponentInit.TRAIT_COMP.get());
        if (xeneComp != null && !xeneComp.isEmpty()) {
            xeneComp.getTraits().forEach(trait -> {
                if (trait != null) {
                    trait.modifyAttributes(event);
                }
            });
        }

        // 2) GeneItem 在 GENE_INSTANCE 组件内的词条
        GeneInstance geneInstance = stack.get(DataComponentInit.GENE_INSTANCE.get());
        if (geneInstance == null) {
            return;
        }

        TraitComp geneComp = geneInstance.getComponents().get(DataComponentInit.TRAIT_COMP.get());
        if (geneComp != null && !geneComp.isEmpty()) {
            geneComp.getTraits().forEach(trait -> {
                if (trait != null) {
                    trait.modifyAttributes(event);
                }
            });
        }
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

            // 1) 收集 XeneItem 直接附着在 ItemStack 上的词条
            TraitComp xeneComp = stack.get(DataComponentInit.TRAIT_COMP.get());
            if (xeneComp != null && !xeneComp.isEmpty()) {
                traits.addAll(xeneComp.getTraits());
            }

            // 2) 收集 GeneItem 在 GENE_INSTANCE 中的词条
            GeneInstance geneInstance = stack.get(DataComponentInit.GENE_INSTANCE.get());
            if (geneInstance == null) {
                continue;
            }

            TraitComp geneComp = geneInstance.getComponents().get(DataComponentInit.TRAIT_COMP.get());
            if (geneComp != null && !geneComp.isEmpty()) {
                traits.addAll(geneComp.getTraits());
            }
        }
    }
}
