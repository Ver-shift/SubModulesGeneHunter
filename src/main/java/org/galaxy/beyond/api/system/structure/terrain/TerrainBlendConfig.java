package org.galaxy.beyond.api.system.structure.terrain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/**
 * Terrain fill settings for {@link TerrainBlendJigsawStructure}.
 *
 * @param radius          Horizontal distance, in blocks, where nearby terrain columns can be checked.
 * @param depth           Template bottom layer depth used to collect structure foundation anchors.
 * @param foundationDepth Minimum foundation fill depth and fluid replacement depth.
 * @param flatnessRadius  Reserved for flatter inner blending. Currently kept for data compatibility.
 * @param maxSurfaceDelta Extra allowed height gap when filling raised structure foundations.
 * @param solidTag        Block tag that marks template blocks as terrain-supporting foundation blocks.
 */
public record TerrainBlendConfig(
        int radius,
        int depth,
        int foundationDepth,
        int flatnessRadius,
        int maxSurfaceDelta,
        Identifier solidTag
) {

    public static final TerrainBlendConfig DEFAULT = new TerrainBlendConfig(
            10,
            6,
            8,
            0,
            8,
            Identifier.fromNamespaceAndPath("beyond", "terrain_blend_solid")
    );

    public static final Codec<TerrainBlendConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 24).optionalFieldOf("radius", DEFAULT.radius()).forGetter(TerrainBlendConfig::radius),
            Codec.intRange(1, 16).optionalFieldOf("depth", DEFAULT.depth()).forGetter(TerrainBlendConfig::depth),
            Codec.intRange(0, 24).optionalFieldOf("foundation_depth", DEFAULT.foundationDepth()).forGetter(TerrainBlendConfig::foundationDepth),
            Codec.intRange(0, 96).optionalFieldOf("flatness_radius", DEFAULT.flatnessRadius()).forGetter(TerrainBlendConfig::flatnessRadius),
            Codec.intRange(1, 32).optionalFieldOf("max_surface_delta", DEFAULT.maxSurfaceDelta()).forGetter(TerrainBlendConfig::maxSurfaceDelta),
            Identifier.CODEC.optionalFieldOf("solid_tag", DEFAULT.solidTag()).forGetter(TerrainBlendConfig::solidTag)
    ).apply(instance, TerrainBlendConfig::new));
}
