package org.galaxy.gene_hunter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.effect.SlashWaveEntity;

public final class SlashWaveRenderer extends EntityRenderer<SlashWaveEntity> {
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(GeneHunter.asResource("slash_gust"), "main");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "textures/entity/trident_riptide.png");
    private final ModelPart body;

    public SlashWaveRenderer(EntityRendererProvider.Context context) {
        super(context);
        body = context.bakeLayer(MODEL_LAYER).getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.0F, -16.0F, -8.0F, 16.0F, 32.0F, 16.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(SlashWaveEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBoundingBox().getYsize() * 0.5D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot() - 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getXRot() - 90.0F));
        poseStack.scale(0.25F, 0.25F, 0.25F);

        float age = entity.tickCount + partialTick;
        float scale = Mth.lerp(Mth.clamp(age / 6.0F, 0.0F, 1.0F), 1.0F, 2.3F);
        float alpha = 1.0F - age / 10.0F;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        for (int index = 0; index < 3; index++) {
            poseStack.mulPose(Axis.YP.rotationDegrees(age * 10.0F));
            poseStack.scale(scale, scale, scale);
            poseStack.translate(0.0F, scale - 1.0F, 0.0F);
            body.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                    FastColor.ARGB32.color((int) (alpha * 255.0F), 255, 255, 255));
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SlashWaveEntity entity) {
        return TEXTURE;
    }
}
