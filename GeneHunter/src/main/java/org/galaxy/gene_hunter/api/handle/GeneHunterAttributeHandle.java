package org.galaxy.gene_hunter.api.handle;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.init.GeneHunterTags;

@EventBusSubscriber
public class GeneHunterAttributeHandle {

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event){
        var oldDamage = event.getOriginalDamage();
        var damageSource = event.getSource();

        /**
         * 只让玩家生效减小性能负担，
         * 同时只给attack伤害类型增伤，防止不同的伤害互相叠加出现错误
         */
        if (damageSource.is(DamageTypes.PLAYER_ATTACK) &&
                damageSource.getEntity() instanceof ServerPlayer player){

            event.setNewDamage(handle(oldDamage, player));





        }
    }


    /**
     * 提供伤害
     * @param oldDamage
     * @param player
     * @return
     */
    public static float handle(float oldDamage,ServerPlayer player){
        AttributeInstance one_hand = player.getAttribute(GeneHunterAttributeInit.ONE_HAND_WEAPON_DAMAGE);
        if (player.getMainHandItem().is(GeneHunterTags.ONE_HAND_WEAPON) || player.getOffhandItem().is(GeneHunterTags.ONE_HAND_WEAPON)){
            return (float) (oldDamage + one_hand.getValue());
        } else if (player.getMainHandItem().is(GeneHunterTags.TWO_HAND_WEAPON) || player.getOffhandItem().is(GeneHunterTags.TWO_HAND_WEAPON)) {
            AttributeInstance two_hand = player.getAttribute(GeneHunterAttributeInit.TWO_HAND_WEAPON_DAMAGE);
            return (float) (oldDamage + two_hand.getValue());
        } else if (player.getMainHandItem().is(GeneHunterTags.POLEARM_WEAPON) || player.getOffhandItem().is(GeneHunterTags.POLEARM_WEAPON)){
            AttributeInstance polearm = player.getAttribute(GeneHunterAttributeInit.POLEARM_WEAPON_DAMAGE);
            return (float) (oldDamage + polearm.getValue());
        }
        return oldDamage;
    }



}
