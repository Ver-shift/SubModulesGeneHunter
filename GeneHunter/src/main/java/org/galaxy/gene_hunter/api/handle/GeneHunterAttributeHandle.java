package org.galaxy.gene_hunter.api.handle;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.galaxy.gene_hunter.attribute.WeaponDamageHandler;

@EventBusSubscriber
public class GeneHunterAttributeHandle {

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        var oldDamage = event.getOriginalDamage();
        var damageSource = event.getSource();

        // 只处理玩家攻击，避免其他伤害类型误吃武器增伤。
        if (damageSource.is(DamageTypes.PLAYER_ATTACK) &&
                damageSource.getEntity() instanceof ServerPlayer player) {

            event.setNewDamage(handle(oldDamage, player));


        }
    }


    public static float handle(float oldDamage, ServerPlayer player) {
        return WeaponDamageHandler.handle(oldDamage, player);
    }


}
