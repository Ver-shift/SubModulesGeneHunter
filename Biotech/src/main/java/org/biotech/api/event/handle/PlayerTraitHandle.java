package org.biotech.api.event.handle;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.biotech.Biotech;
import org.biotech.api.BiotechAPI;
import org.biotech.api.init.BiotechTraitInit;
import org.biotech.api.system.gene.GeneContext;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.api.system.trait.core.IStackTraitAccess;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Biotech.MODID)
public class PlayerTraitHandle {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GeneContext context = new GeneContext(player);
            getPlayerTraitCounts(player).forEach((trait, traitCount) ->
                    trait.tick(context, traitCount)
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GeneContext context = new GeneContext(player);
            getPlayerTraitCounts(player).forEach((trait, traitCount) ->
                    trait.attack(context, event.getTarget(), traitCount)
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GeneContext context = new GeneContext(player);
            getPlayerTraitCounts(player).forEach((trait, traitCount) ->
                    trait.jump(context, traitCount)
            );
        }
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        refreshTraitAttributes(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refreshTraitAttributes(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        clearTraitAttributes(event.getOriginal());
        if (event.getEntity() instanceof ServerPlayer player) {
            refreshTraitAttributes(player);
        }
    }


    /**
     * 获取玩家基因相关槽位（gene/xene）中物品携带的词条。
     */
    private static List<ITrait> getPlayerTraits(Player player) {
        List<ITrait> traits = new ArrayList<>();
        collectTraitsFromSlots(BiotechAPI.getGeneEquipSlots(player), traits);
        collectTraitsFromSlots(BiotechAPI.getXeneEquipSlots(player), traits);
        return traits;
    }

    private static Map<ITrait, Integer> getPlayerTraitCounts(Player player) {
        return countTraits(getPlayerTraits(player));
    }

    private static void refreshTraitAttributes(Player player) {
        clearTraitAttributes(player);
        getPlayerTraitCounts(player).forEach((trait, traitCount) ->
                trait.modifyAttributes(player, traitCount)
        );
    }

    private static void clearTraitAttributes(Player player) {
        BiotechTraitInit.getAllTraits().forEach(trait -> trait.removeAttributes(player));
    }

    private static Map<ITrait, Integer> countTraits(List<ITrait> traits) {
        Map<ResourceLocation, TraitCount> counts = new LinkedHashMap<>();
        for (ITrait trait : traits) {
            if (trait == null || trait.getId() == null || ITrait.EMPTY_TRAIT_ID.equals(trait.getId())) {
                continue;
            }
            counts.compute(trait.getId(), (id, count) -> count == null ? new TraitCount(trait, 1) : count.add());
        }

        Map<ITrait, Integer> result = new LinkedHashMap<>();
        counts.values().forEach(count -> result.put(count.trait(), count.count()));
        return result;
    }

    private record TraitCount(ITrait trait, int count) {

        private TraitCount add() {
            return new TraitCount(trait, count + 1);
        }
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
