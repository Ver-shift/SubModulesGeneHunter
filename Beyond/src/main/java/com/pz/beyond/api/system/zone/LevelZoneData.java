package com.pz.beyond.api.system.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.config.ServerConfig;
import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.zone.core.IZonePosManager;
import com.pz.beyond.api.system.zone.zones.PendingPlayerActiveZone;
import com.pz.beyond.api.system.zone.zones.SafeZone;
import lombok.Data;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 世界区域数据，挂载在 Level 上
 */
@Data
public class LevelZoneData implements IZonePosManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LevelZoneData.class);

    public static final String DIMENSION = "dimension";
    public static final String ZONE_DATA = "zone_data";
    public static final String SAFE_CENTER_CHUNK_KEY = "safe_center_chunk_key";
    public static final String SAFE_ZONE_INITIALIZED = "safe_zone_initialized";
    public static final String PENDING_ZONE_INITIALIZED = "pending_zone_initialized";

    public static final Codec<LevelZoneData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        ResourceLocation.CODEC.fieldOf(DIMENSION).forGetter(LevelZoneData::getDimensionId),
        ZoneData.CODEC.listOf().optionalFieldOf(ZONE_DATA, List.of()).forGetter(LevelZoneData::getZoneData),
        Codec.LONG.optionalFieldOf(SAFE_CENTER_CHUNK_KEY, ChunkPos.asLong(0, 0)).forGetter(LevelZoneData::getSafeCenterChunkKey),
        Codec.BOOL.optionalFieldOf(SAFE_ZONE_INITIALIZED, false).forGetter(LevelZoneData::isSafeZoneInitialized),
        Codec.BOOL.optionalFieldOf(PENDING_ZONE_INITIALIZED, false).forGetter(LevelZoneData::isPendingZoneInitialized)
    ).apply(builder, LevelZoneData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LevelZoneData> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        LevelZoneData::getDimensionId,
        ByteBufCodecs.collection(ArrayList::new, ZoneData.STREAM_CODEC),
        LevelZoneData::getZoneData,
        ByteBufCodecs.VAR_LONG,
        LevelZoneData::getSafeCenterChunkKey,
        ByteBufCodecs.BOOL,
        LevelZoneData::isSafeZoneInitialized,
        ByteBufCodecs.BOOL,
        LevelZoneData::isPendingZoneInitialized,
        LevelZoneData::new
    );

    private ResourceKey<Level> dimension = Level.OVERWORLD;
    //就这么几种类型，不过度设计了
    private List<ZoneData> zoneData = new ArrayList<>();
    private long safeCenterChunkKey = ChunkPos.asLong(0, 0);
    private boolean safeZoneInitialized;
    private boolean pendingZoneInitialized;

    /**
     * 无参构造（用于 Attachment 创建或反序列化）
     */
    public LevelZoneData() {
        LOGGER.debug("LevelZoneData 无参构造调用");
    }

    /**
     * 带 ServerLevel 的构造（用于首次创建时初始化安全区）
     */
    public LevelZoneData(ServerLevel level) {
        LOGGER.debug("LevelZoneData 带 ServerLevel 构造调用, dimension={}", level.dimension().location());
        this.dimension = level.dimension();
    }

    public LevelZoneData(ResourceLocation dimension, List<ZoneData> zoneData) {
        this.dimension = ResourceKey.create(Registries.DIMENSION, dimension);
        this.zoneData = zoneData == null ? new ArrayList<>() : new ArrayList<>(zoneData);
    }

    public LevelZoneData(ResourceLocation dimension, List<ZoneData> zoneData, long safeCenterChunkKey, boolean safeZoneInitialized) {
        this(dimension, zoneData, safeCenterChunkKey, safeZoneInitialized, false);
    }

    public LevelZoneData(ResourceLocation dimension, List<ZoneData> zoneData, long safeCenterChunkKey, boolean safeZoneInitialized, boolean pendingZoneInitialized) {
        this.dimension = ResourceKey.create(Registries.DIMENSION, dimension);
        this.zoneData = zoneData == null ? new ArrayList<>() : new ArrayList<>(zoneData);
        this.safeCenterChunkKey = safeCenterChunkKey;
        this.safeZoneInitialized = safeZoneInitialized;
        this.pendingZoneInitialized = pendingZoneInitialized;
    }

    public ResourceLocation getDimensionId() {
        return dimension.location();
    }


    public void addListener(ResourceLocation zoneId, RuleData ruleData) {
        if (ruleData == null) {
            return;
        }
        ZoneData zone = getOrCreateZone(zoneId);
        zone.addListener(ruleData);
    }


    @Override
    public void addSafeZone(int chunkCountSize) {
        int squareSize = Math.max(1, chunkCountSize);
        ChunkPos center = new ChunkPos(safeCenterChunkKey);
        int half = (squareSize - 1) / 2;

        ZoneData safeZone = getOrCreateZone(SafeZone.SAFE_ZONE);
        safeZone.getChunkKeys().clear();

        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                safeZone.getChunkKeys().add(ChunkPos.asLong(center.x + dx, center.z + dz));
            }
        }
        safeZoneInitialized = true;
    }

    @Override
    public void spawnZone(ChunkPos chunkPos) {

    }

    @Override
    public void addPlayerActiveZone() {

    }

    @Override
    public void addPendingPlayerZone() {

    }

    /**
     * 尝试初始化安全区（仅在世界首次加载时执行一次）
     */
    public void initializeSafeZoneIfNeeded(ServerLevel level) {
        LOGGER.debug("initializeSafeZoneIfNeeded 开始, safeZoneInitialized={}", safeZoneInitialized);
        if (safeZoneInitialized) {
            LOGGER.debug("安全区已初始化，跳过");
            return;
        }
        if (!ServerConfig.enableVillageBootstrap()) {
            LOGGER.debug("村庄引导功能未启用，跳过");
            return;
        }

        // 调用 SafeZone 进行初始化
        SafeZone.initialize(level, this);
    }

    public void setSafeCenterChunk(ChunkPos centerChunk) {
        this.safeCenterChunkKey = centerChunk.toLong();
    }

    public ChunkPos getSafeCenterChunk() {
        return new ChunkPos(safeCenterChunkKey);
    }

    private ZoneData getOrCreateZone(ResourceLocation zoneId) {
        for (ZoneData data : zoneData) {
            if (zoneId.equals(data.getZoneId())) {
                return data;
            }
        }
        // 创建新的 ZoneData
        AbstractZone<?> zone = BeyondZoneInit.getZoneById(zoneId);
        ZoneData created = new ZoneData(zone);
        zoneData.add(created);
        return created;
    }

    /**
     * 获取或创建区域数据，并在首次创建时调用初始化方法
     * 
     * @param zoneId 区域ID
     * @param level 服务端维度（用于初始化）
     * @return ZoneData
     */
    public ZoneData getOrCreateZone(ResourceLocation zoneId, ServerLevel level) {
        for (ZoneData data : zoneData) {
            if (zoneId.equals(data.getZoneId())) {
                // 找到已存在的 ZoneData，检查是否需要初始化
                if (!data.isInitialized() && level != null) {
                    data.getZone().initialize(data.getListeners(), level);
                    data.setInitialized(true);
                }
                return data;
            }
        }
        // 创建新的 ZoneData
        AbstractZone<?> zone = BeyondZoneInit.getZoneById(zoneId);
        ZoneData created = new ZoneData(zone);
        zoneData.add(created);
        
        // 调用初始化方法（仅首次创建时）
        if (!created.isInitialized() && level != null) {
            zone.initialize(created.getListeners(), level);
            created.setInitialized(true);
        }
        
        return created;
    }

    /**
     * 根据区块 key 查找对应的区域
     * @param chunkKey 区块 key (ChunkPos.toLong())
     * @return 包含该区块的区域，如果没有找到返回 PendingZone
     */
    public AbstractZone<?> getZoneByChunkKey(long chunkKey) {
        for (ZoneData data : zoneData) {
            if (data.getChunkKeys().contains(chunkKey)) {
                return data.getZone();
            }
        }
        // 不属于任何已定义区域的区块，返回 PendingZone
        return BeyondZoneInit.getZoneById(PendingPlayerActiveZone.PENDING_PLAYER_ACTIVE_ZONE);
    }

    /**
     * 获取或创建 PendingPlayerActiveZone 数据
     */
    public ZoneData getOrCreatePendingZone(ServerLevel level) {
        return getOrCreateZone(PendingPlayerActiveZone.PENDING_PLAYER_ACTIVE_ZONE, level);
    }

    /**
     * 将区块添加到 PendingZone（如果该区块不属于安全区）
     * 
     * @param chunkKey 区块 key
     * @return true 如果成功添加，false 如果该区块属于安全区
     */
    public boolean addChunkToPendingZone(long chunkKey) {
        // 检查是否属于安全区
        ZoneData safeZone = getOrCreateZone(SafeZone.SAFE_ZONE);
        if (safeZone.getChunkKeys().contains(chunkKey)) {
            return false; // 安全区区块不添加到 PendingZone
        }
        
        // 添加到 PendingZone
        ZoneData pendingZone = getOrCreateZone(PendingPlayerActiveZone.PENDING_PLAYER_ACTIVE_ZONE);
        pendingZone.getChunkKeys().add(chunkKey);
        return true;
    }

    /**
     * 批量添加区块到 PendingZone
     * 
     * @param chunkKeys 区块 key 集合
     * @return 添加的区块数量
     */
    public int addChunksToPendingZone(Iterable<Long> chunkKeys) {
        int count = 0;
        for (Long chunkKey : chunkKeys) {
            if (addChunkToPendingZone(chunkKey)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 初始化 PendingZone（将所有已加载区块添加到 PendingZone）
     * 
     * @param level 服务端维度
     */
    public void initializePendingZone(ServerLevel level) {
        if (pendingZoneInitialized) {
            return;
        }

        LOGGER.debug("开始初始化 PendingZone");
        
        // 获取安全区的区块集合
        ZoneData safeZone = getOrCreateZone(SafeZone.SAFE_ZONE);
        it.unimi.dsi.fastutil.longs.LongOpenHashSet safeChunkKeys = safeZone.getChunkKeys();
        
        // 获取服务器已加载的区块
        Iterable<net.minecraft.world.level.chunk.ChunkAccess> loadedChunks = level.getChunkSource().getLoadedChunks();
        int count = 0;
        for (net.minecraft.world.level.chunk.ChunkAccess chunk : loadedChunks) {
            long chunkKey = chunk.getPos().toLong();
            // 不属于安全区的区块添加到 PendingZone
            if (!safeChunkKeys.contains(chunkKey)) {
                ZoneData pendingZone = getOrCreateZone(PendingPlayerActiveZone.PENDING_PLAYER_ACTIVE_ZONE, level);
                pendingZone.getChunkKeys().add(chunkKey);
                count++;
            }
        }
        
        pendingZoneInitialized = true;
        LOGGER.info("PendingZone 初始化完成，添加了 {} 个区块", count);
    }
}
