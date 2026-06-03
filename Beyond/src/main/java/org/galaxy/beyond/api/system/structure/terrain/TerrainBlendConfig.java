package org.galaxy.beyond.api.system.structure.terrain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Terrain fill settings for {@link TerrainBlendJigsawStructure}.
 *
 * @param radius          Core horizontal blend distance, in blocks.
 * @param depth           Template bottom layer depth used to collect structure foundation anchors.
 * @param foundationDepth Minimum foundation fill depth and fluid replacement depth.
 * @param flatnessRadius  Reserved for flatter inner blending. Currently kept for data compatibility.
 * @param maxSurfaceDelta Extra allowed height gap when filling raised structure foundations.
 * @param blendExtension  Extra outward blend distance used for soft terrain shoulders.
 * @param slopeHeight     Maximum height added to nearby terrain shoulder columns.
 */
public record TerrainBlendConfig(
        int radius,
        int depth,
        int foundationDepth,
        int flatnessRadius,
        int maxSurfaceDelta,
        int blendExtension,
        int slopeHeight
) {

    public static final TerrainBlendConfig DEFAULT = new TerrainBlendConfig(
            10,
            6,
            8,
            0,
            8,
            8,
            4
    );

    public static final Codec<TerrainBlendConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 24).optionalFieldOf("radius", DEFAULT.radius()).forGetter(TerrainBlendConfig::radius),
            Codec.intRange(1, 16).optionalFieldOf("depth", DEFAULT.depth()).forGetter(TerrainBlendConfig::depth),
            Codec.intRange(0, 24).optionalFieldOf("foundation_depth", DEFAULT.foundationDepth()).forGetter(TerrainBlendConfig::foundationDepth),
            Codec.intRange(0, 96).optionalFieldOf("flatness_radius", DEFAULT.flatnessRadius()).forGetter(TerrainBlendConfig::flatnessRadius),
            Codec.intRange(1, 32).optionalFieldOf("max_surface_delta", DEFAULT.maxSurfaceDelta()).forGetter(TerrainBlendConfig::maxSurfaceDelta),
            Codec.intRange(0, 32).optionalFieldOf("blend_extension", DEFAULT.blendExtension()).forGetter(TerrainBlendConfig::blendExtension),
            Codec.intRange(0, 16).optionalFieldOf("slope_height", DEFAULT.slopeHeight()).forGetter(TerrainBlendConfig::slopeHeight)
    ).apply(instance, TerrainBlendConfig::new));

    public int maxAllowedSurfaceDrop() {
        return foundationDepth + maxSurfaceDelta + radius;
    }

    public int blendRadius() {
        return radius + blendExtension;
    }
}
