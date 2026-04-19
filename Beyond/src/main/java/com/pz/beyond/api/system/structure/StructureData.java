package com.pz.beyond.api.system.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

/**
 * 结构数据存储类
 * <p>
 * 存储世界安全出生点坐标，用于：
 * - 世界加载时查找并缓存安全村庄位置
 * - 玩家首次登录时设置出生点和传送目标
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StructureData {

    // 常量定义
    public static final String SPAWN_POS = "spawn_pos";
    public static final String INITIALIZED = "initialized";

    /**
     * 安全出生点坐标
     * 在绿色平原群系的村庄附近找到的安全位置
     */
    private BlockPos spawnPos = BlockPos.ZERO;

    /**
     * 是否已完成初始化
     * 防止重复搜索和设置
     */
    private boolean initialized = false;

    

    /**
     * 检查是否已设置有效的出生点
     */
    public boolean hasValidSpawnPos() {
        return initialized && spawnPos != null && !spawnPos.equals(BlockPos.ZERO);
    }

    /**
     * 设置出生点坐标
     */
    public void setSpawnPos(BlockPos pos) {
        this.spawnPos = pos != null ? pos.immutable() : BlockPos.ZERO;
        this.initialized = true;
    }

    // Codec 编解码器
    public static final Codec<StructureData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.optionalFieldOf(SPAWN_POS, BlockPos.ZERO).forGetter(StructureData::getSpawnPos),
            Codec.BOOL.optionalFieldOf(INITIALIZED, false).forGetter(StructureData::isInitialized)
    ).apply(instance, StructureData::new));

    // StreamCodec 网络传输编解码器
    public static final StreamCodec<RegistryFriendlyByteBuf, StructureData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            StructureData::getSpawnPos,
            net.minecraft.network.codec.ByteBufCodecs.BOOL,
            StructureData::isInitialized,
            StructureData::new
    );
}
