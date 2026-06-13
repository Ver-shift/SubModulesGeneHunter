package org.galaxy.gene_hunter.mixin;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.gene_hunter.gateway.GeneHunterDynamicGatewayManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GatewayEntity.class)
public class GatewayEntityPersistenceMixin {

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void geneHunter$restoreDynamicGateway(CompoundTag tag, CallbackInfo ci) {
        if (!tag.contains("gate", Tag.TAG_STRING)) return;
        if (!(((GatewayEntity) (Object) this).level() instanceof ServerLevel level)) return;
        GeneHunterDynamicGatewayManager.ensureAvailable(level, ResourceLocation.parse(tag.getString("gate")));
    }
}
