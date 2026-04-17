package com.pz.beyond.api.system.zone.zones;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.config.ServerConfig;
import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.init.BeyondZoneRuleInit;
import com.pz.beyond.api.system.progress.ProgressCatalog;
import com.pz.beyond.api.system.progress.ProgressState;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.zone.AbstractZone;
import com.pz.beyond.api.system.zone.LevelZoneData;
import com.pz.beyond.api.util.BorderRenderUtil;
import com.pz.beyond.api.util.TeleportUtil;
import net.minecraft.client.Camera;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 安全区域
 * <p>
 * 负责安全区的初始化和玩家传送
 */
@EventBusSubscriber
public class SafeZone extends AbstractZone<Void> {

    public static final ResourceLocation SAFE_ZONE = Beyond.asResource("safe_zone");
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeZone.class);

    public SafeZone() {
        super(SAFE_ZONE);
    }

    /**
     * 初始化安全区规则
     * 添加默认的规则监听器
     * 
     * @param listeners 规则监听器列表
     * @param level 服务端维度
     */
    @Override
    public void initialize(List<RuleData> listeners, ServerLevel level) {
        LOGGER.debug("SafeZone 初始化规则");
        // 添加安全区规则：全部安全
        addRule(listeners, new RuleData(BeyondZoneRuleInit.ALL_SAFE_RULE.get()));
    }

    /**
     * 初始化安全区（村庄查找、玩家传送等）
     * <p>
     * 查找村庄、设置出生点、传送玩家、创建安全区
     *
     * @param level    服务器世界
     * @param zoneData 区域数据
     */
    public static void initialize(ServerLevel level, LevelZoneData zoneData) {
        LOGGER.debug("开始初始化安全区");

        // 查找村庄位置
        BlockPos villagePos = TeleportUtil.findNearestStructure(
            level,
            StructureTags.VILLAGE,
            ServerConfig.villageSearchRadiusChunks(),
            level.getSharedSpawnPos()
        );
        LOGGER.debug("找到村庄位置: {}", villagePos);

        // 获取地面位置
        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, villagePos);
        LOGGER.debug("地面位置: {}", surfacePos);

        // 设置世界出生点
        if (ServerConfig.setWorldSpawn()) {
            level.setDefaultSpawnPos(surfacePos, 0.0F);
            LOGGER.debug("设置世界出生点: {}", surfacePos);
        }

        // 传送玩家
        teleportPlayersToSafeZone(level, surfacePos);

        // 创建安全区
        createSafeZoneChunks(zoneData, surfacePos, level);

        // 触发规则初始化（调用带 level 参数的版本）
        zoneData.getOrCreateZone(SAFE_ZONE, level);

        // 更新 ProgressCatalog 状态
        updateProgressState(level);

        LOGGER.info("安全区初始化完成，中心: {}", surfacePos);
    }

    /**
     * 传送所有玩家到安全区
     *
     * @param level      服务器世界
     * @param targetPos  目标位置（村庄地面）
     */
    private static void teleportPlayersToSafeZone(ServerLevel level, BlockPos targetPos) {
        if (!ServerConfig.teleportPlayersOnLoad()) {
            return;
        }

        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            return;
        }

        // 查找安全的传送位置
        BlockPos safePos = TeleportUtil.findSafeTeleportPos(level, targetPos);
        LOGGER.debug("安全传送位置: {}", safePos);

        for (ServerPlayer player : players) {
            // 发送传送提示消息
            player.sendSystemMessage(Component.translatable("beyond.safe_zone.teleporting"));
            // 传送到安全位置
            player.teleportTo(
                level,
                safePos.getX() + 0.5D,
                safePos.getY(),
                safePos.getZ() + 0.5D,
                player.getYRot(),
                player.getXRot()
            );
        }

        LOGGER.debug("传送 {} 个玩家到安全区", players.size());
    }

    /**
     * 创建安全区区块
     * <p>
     * 算法：获取村庄结构的边界框，计算最小包围正方形，再向外延伸1个区块
     *
     * @param zoneData   区域数据
     * @param centerPos  中心位置（村庄位置）
     * @param level      服务器世界
     */
    private static void createSafeZoneChunks(LevelZoneData zoneData, BlockPos centerPos, ServerLevel level) {
        // 计算基于村庄结构的最小安全区大小
        int safeZoneSize = calculateSafeZoneSizeFromVillage(level, centerPos);

        zoneData.setSafeCenterChunk(new ChunkPos(centerPos));
        zoneData.addSafeZone(safeZoneSize);

        LOGGER.debug("创建安全区，大小: {} 区块", safeZoneSize);
    }

    /**
     * 基于村庄结构计算安全区大小
     * <p>
     * 1. 获取村庄结构的边界框
     * 2. 计算覆盖整个结构的最小正方形
     * 3. 向外延伸1个区块作为缓冲
     *
     * @param level      服务器世界
     * @param villagePos 村庄位置
     * @return 安全区大小（区块数）
     */
    private static int calculateSafeZoneSizeFromVillage(ServerLevel level, BlockPos villagePos) {
        // 获取村庄结构的起始点
        SectionPos sectionPos = SectionPos.of(villagePos);
        StructureStart villageStart = level.structureManager().getStructureWithPieceAt(villagePos, StructureTags.VILLAGE);

        int minSize = Math.max(
            ServerConfig.initialSafeZoneSizeChunks(),
            ServerConfig.minVillageWrapSizeChunks()
        );

        if (villageStart == null || !villageStart.isValid()) {
            LOGGER.debug("无法获取村庄结构信息，使用配置的最小大小: {}", minSize);
            return minSize;
        }

        // 获取村庄结构的边界框
        var boundingBox = villageStart.getBoundingBox();

        // 计算边界框的宽度和深度（区块）
        int minX = SectionPos.blockToSectionCoord(boundingBox.minX());
        int maxX = SectionPos.blockToSectionCoord(boundingBox.maxX());
        int minZ = SectionPos.blockToSectionCoord(boundingBox.minZ());
        int maxZ = SectionPos.blockToSectionCoord(boundingBox.maxZ());

        int widthChunks = maxX - minX + 1;
        int depthChunks = maxZ - minZ + 1;

        // 取长边作为基础大小
        int baseSize = Math.max(widthChunks, depthChunks);

        // 向外延伸1个区块作为缓冲
        int finalSize = baseSize + 2;  // 两边各延伸1个区块

        // 确保不小于配置的最小大小
        finalSize = Math.max(finalSize, minSize);

        LOGGER.debug("村庄结构边界: 宽度={} 区块, 深度={} 区块, 计算的安全区大小={} 区块",
            widthChunks, depthChunks, finalSize);

        return finalSize;
    }

    /**
     * 更新进度状态为 SAFE
     *
     * @param level 服务器世界
     */
    private static void updateProgressState(ServerLevel level) {
        ProgressCatalog progressCatalog = BeyondAttachInit.getProgressCatalog(level);
        if (progressCatalog != null) {
            progressCatalog.setProgressState(ProgressState.SAFE);
            LOGGER.debug("ProgressCatalog 状态已设置为 SAFE");
        }
    }


    // 渲染逻辑
    private static final ResourceLocation FORCEFIELD = ResourceLocation.parse("minecraft:textures/misc/forcefield.png");

    @SubscribeEvent
    public static void onRenderLevelStageEvent(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Level level = event.getCamera().getEntity().level();
        if (!level.isClientSide()) {
            return;
        }

        // 获取区域数据
        LevelZoneData zoneData = BeyondAPI.getZoneData(level);
        if (zoneData == null || !zoneData.isSafeZoneInitialized()) {
            return;
        }

        // 获取安全区中心区块
        ChunkPos centerChunk = zoneData.getSafeCenterChunk();
        if (centerChunk.equals(new ChunkPos(0, 0))) {
            return;  // 未设置中心
        }

        // 计算渲染参数
        int centerX = (centerChunk.x << 4) + 8;  // 区块转方块坐标（中心）
        int centerZ = (centerChunk.z << 4) + 8;
        int radius = Math.max(
            ServerConfig.initialSafeZoneSizeChunks(),
            ServerConfig.minVillageWrapSizeChunks()
        ) << 4;  // 区块转方块

        if (radius <= 0) {
            return;
        }

        // 科技蓝边界 (0x00BFFF - 深天蓝)
        int color = 0x00BFFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int bottomAlpha = 180;
        int topAlpha = 0;

        Camera camera = event.getCamera();
        PoseStack poseStack = event.getPoseStack();

        BorderRenderUtil.render(centerX, centerZ, radius, r, g, b, bottomAlpha, topAlpha, FORCEFIELD, camera, poseStack);
    }
}
