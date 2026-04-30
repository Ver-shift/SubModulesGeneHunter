package com.pz.beyond.api.system.structure;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.system.BeyondLevelData;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class PlayerStructureManager {

    public void handlePlayerFirstLoad(ServerPlayer serverPlayer){
        // 步骤2: 获取玩家所在的服务器世界
        ServerLevel serverLevel = serverPlayer.serverLevel();

        // 步骤3: 检查维度是否允许（只处理主世界）
        if (!BeyondAttachInit.isAllowedDimension(serverLevel)) {
            Beyond.debugLog("Player {} is not in allowed dimension, skipping spawn setup",
                    serverPlayer.getName().getString());
            return;
        }

        // 步骤4: 获取世界的 BeyondLevelData
        BeyondLevelData levelData = BeyondAPI.getBeyondLevelData(serverLevel);

        // 步骤5: 从 LevelData 获取 StructureData（包含安全出生点坐标）
        StructureData structureData = levelData.getStructureData();

        // 步骤6: 验证 StructureData 是否有有效的出生点
        if (!structureData.hasValidSpawnPos()) {
            Beyond.debugLog("StructureData has no valid spawn pos for player first login: {}",
                    serverPlayer.getName().getString());
            return;
        }

        // 步骤7: 获取安全出生点坐标
        BlockPos spawnPos = structureData.getSpawnPos();
        Beyond.debugLog("Preparing to set spawn for player {} at {}",
                serverPlayer.getName().getString(), spawnPos);

        // 步骤8: 设置玩家的重生点（床/锚点位置）
        // 参数说明: dimension-维度, pos-位置, angle-朝向, forced-强制设置, sendMessage-发送消息
        serverPlayer.setRespawnPosition(
                serverLevel.dimension(),
                spawnPos,
                0.0f,   // 朝向角度
                true,   // forced: 强制设置，覆盖之前的重生点
                false   // sendMessage: 不发送消息给玩家
        );
        Beyond.debugLog("Set respawn position for player {} to {}",
                serverPlayer.getName().getString(), spawnPos);

        // 步骤9: 传送玩家到安全出生点
        // 坐标 +0.5 是为了让玩家站在方块中心，避免卡在边缘
        serverPlayer.teleportTo(
                serverLevel,
                spawnPos.getX() + 0.5,  // X坐标居中
                spawnPos.getY(),        // Y坐标（高度）
                spawnPos.getZ() + 0.5,  // Z坐标居中
                0.0f,   // yaw: 水平朝向
                0.0f    // pitch: 垂直朝向
        );

        //步骤10: 记录完成日志
        Beyond.debugLog("Player {} first login complete: set spawn and teleported to {}",
                serverPlayer.getName().getString(), spawnPos);

        //欢迎游玩基因猎人整合包，寻找村庄中的传送石碑进行初始化世界
        //todo 给传送石碑弄上发光的传送点光柱，加上hud显示。
        Component waystoneName = ModBlocks.waystone.getName().withStyle(net.minecraft.ChatFormatting.GOLD);
        serverPlayer.sendSystemMessage(Component.translatable("message.beyond.welcome", waystoneName));
    }
}
