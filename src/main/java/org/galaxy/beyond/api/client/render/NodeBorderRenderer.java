package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneHelper;
import org.galaxy.beyond.api.system.zone.ZoneType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public class NodeBorderRenderer {

    private static final int VERTICES_PER_BOX = 16;
    private static final int FACES_PER_BOX = 4;

    private GpuBuffer vertexBuffer;
    private RenderSystem.AutoStorageIndexBuffer indices;
    private boolean needsRebuild = true;
    private int lastEntryHash;
    private int totalVertices;
    private List<BoxInfo> boxInfos = List.of();

    private record BoxInfo(ZoneType type, double worldMinX, double worldMinZ, int boxIndex) {}

    private RenderSystem.AutoStorageIndexBuffer getIndices() {
        if (indices == null) {
            indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        }
        return indices;
    }

    public void render(Level level, Vec3 cameraPos, PoseStack poseStack) {
        if (level == null) return;

        var dimData = BeyondAPI.getBeyondDimensionData(level);
        if (dimData == null) return;

        LevelZoneData zoneData = dimData.getLevelZoneData();
        if (zoneData == null) return;

        Set<Map.Entry<ChunkPos, ZoneType>> entries = zoneData.getZoneEntries();
        if (!zoneData.hasZones()) return;

        int currentHash = entries.hashCode();
        if (needsRebuild || currentHash != lastEntryHash) {
            rebuild(entries);
            lastEntryHash = currentHash;
            needsRebuild = false;
        }

        if (boxInfos.isEmpty()) return;

        var ctx = RenderHelper.captureRenderContext();
        var indices = getIndices();
        GpuBuffer indexBuffer = indices.getBuffer(totalVertices);
        float offset = (float) (System.currentTimeMillis() % 3000L) / 3000.0F;

        for (BoxInfo box : boxInfos) {
            float[] rgb = colorFor(box.type());
            float red = rgb[0], green = rgb[1], blue = rgb[2], alpha = rgb[3];

            GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                    .writeTransform(
                            RenderSystem.getModelViewMatrix(),
                            new Vector4f(red, green, blue, alpha),
                            new Vector3f((float) (box.worldMinX() - cameraPos.x),
                                    (float) -cameraPos.y,
                                    (float) (box.worldMinZ() - cameraPos.z)),
                            new Matrix4f().translation(offset, offset, 0.0F)
                    );

            try (RenderPass renderPass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(() -> "Node border " + box.type().getName(),
                            ctx.colorTarget(), OptionalInt.empty(),
                            ctx.depthTarget(), OptionalDouble.empty())) {
                RenderHelper.bindPassState(renderPass, ctx, dynamicTransforms, indexBuffer, indices, this.vertexBuffer);

                List<RenderPass.Draw<NodeBorderRenderer>> draws = new ArrayList<>(FACES_PER_BOX);
                for (int side = 0; side < FACES_PER_BOX; side++) {
                    int faceIndex = (box.boxIndex() * FACES_PER_BOX + side) * 6;
                    draws.add(new RenderPass.Draw<>(0, this.vertexBuffer, indexBuffer, indices.type(), faceIndex, 6, 0));
                }
                renderPass.drawMultipleIndexed(draws, null, null, Collections.emptyList(), this);
            }
        }
    }

    private static float[] colorFor(ZoneType type) {
        return switch (type) {
            case Safe_Zone -> new float[]{64 / 255.0F, 120 / 255.0F, 220 / 255.0F, 0.55F};  // blue
            case Node_Zone -> new float[]{255 / 255.0F, 165 / 255.0F, 0 / 255.0F, 0.55F};    // orange
            case Active_Zone -> new float[]{255 / 255.0F, 255 / 255.0F, 255 / 255.0F, 0.15F}; // faint white
            case Empty -> new float[]{180 / 255.0F, 180 / 255.0F, 180 / 255.0F, 0.55F};       // gray
        };
    }

    private void rebuild(Set<Map.Entry<ChunkPos, ZoneType>> entries) {
        // 按 ZoneType 分组，固定顺序
        Map<ZoneType, Set<ChunkPos>> grouped = new TreeMap<>(Comparator.comparing(ZoneType::ordinal));
        for (var e : entries) {
            grouped.computeIfAbsent(e.getValue(), k -> new HashSet<>()).add(e.getKey());
        }

        // 每个类型的每个连通分量 → 独立的包围盒
        List<BoxInfo> infos = new ArrayList<>();
        List<ZoneHelper.Bounds> allBounds = new ArrayList<>();

        for (var groupEntry : grouped.entrySet()) {
            ZoneType type = groupEntry.getKey();
            var components = ZoneHelper.findConnectedComponents(groupEntry.getValue());
            for (Set<ChunkPos> comp : components) {
                ZoneHelper.Bounds b = ZoneHelper.boundsOf(comp);
                allBounds.add(b);
                infos.add(new BoxInfo(type, b.minX() * 16.0, b.minZ() * 16.0, infos.size()));
            }
        }

        this.boxInfos = List.copyOf(infos);

        // 构建顶点 buffer
        totalVertices = allBounds.size() * VERTICES_PER_BOX;
        int vertexSize = DefaultVertexFormat.POSITION_TEX.getVertexSize();
        int totalBoxes = allBounds.size();

        if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
        }
        this.vertexBuffer = RenderSystem.getDevice()
                .createBuffer(() -> "Node border vbo",
                        GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                        (long) totalVertices * vertexSize);

        try (ByteBufferBuilder byteBuf = ByteBufferBuilder.exactlySized(totalVertices * vertexSize)) {
            BufferBuilder builder = new BufferBuilder(byteBuf, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            float top = 320.0F, bottom = -64.0F;
            float halfHeight = (top - bottom) * 0.5F;

            for (ZoneHelper.Bounds b : allBounds) {
                double bx1 = b.minX() * 16.0, bz1 = b.minZ() * 16.0;
                double bx2 = (b.maxX() + 1) * 16.0, bz2 = (b.maxZ() + 1) * 16.0;
                float width = (float) (bx2 - bx1);
                float depth = (float) (bz2 - bz1);
                RenderHelper.writeBoxWalls(builder, width, depth, halfHeight);
            }

            try (MeshData meshData = builder.buildOrThrow()) {
                RenderSystem.getDevice().createCommandEncoder()
                        .writeToBuffer(this.vertexBuffer.slice(), meshData.vertexBuffer());
            }
        }
    }

    public void invalidate() {
        this.needsRebuild = true;
    }
}
