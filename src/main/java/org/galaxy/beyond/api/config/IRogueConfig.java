package org.galaxy.beyond.api.config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.UUID;

/**
 * 供管理员进行简易设置。全部都要注册指令
 * 配置文件为主。局内可以临时设置。
 * 每次重启服务器数据都会重新专属为配置文件配置
 */
public interface IRogueConfig {

    // ---- 配置文件可覆盖 ----

    void setDebugMode(Boolean enabled);
    boolean isDebugMode();

    void setCurrentProgress(Identifier progressId);
    Identifier getCurrentProgress();

    void setSafeZoneSize(Integer chunkSize);
    int getSafeZoneSize();

    void setMinNodeExpandCount(Integer count);
    int getMinNodeExpandCount();

    void setMinNodeExpandChunks(Integer chunks);
    int getMinNodeExpandChunks();

    void setMaxNodeExpandRange(Integer chunks);
    int getMaxNodeExpandRange();

    void setRogueDimension(ResourceKey<Level> dimension);
    ResourceKey<Level> getRogueDimension();

    /** 清空所有运行时值（含玩家列表），恢复为 CommonConfig 默认值 */
    void reset();

    // ---- 纯持久化（配置文件无法表达，重启后保留） ----

    /** 肉鸽系统内的玩家（不在安全区） */
    Set<UUID> getRoguePlayerIds();
    boolean addRoguePlayer(UUID playerId);
    boolean removeRoguePlayer(UUID playerId);
    boolean isRoguePlayer(UUID playerId);

    /** 安全区内的玩家 */
    Set<UUID> getSafeZonePlayerIds();
    boolean addSafeZonePlayer(UUID playerId);
    boolean removeSafeZonePlayer(UUID playerId);
    boolean isSafeZonePlayer(UUID playerId);
}
