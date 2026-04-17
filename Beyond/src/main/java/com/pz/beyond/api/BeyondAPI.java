package com.pz.beyond.api;

import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.system.BeyondData;
import com.pz.beyond.api.system.progress.ProgressCatalog;
import com.pz.beyond.api.system.progress.ProgressManager;
import com.pz.beyond.api.system.progress.core.IProgressManager;
import com.pz.beyond.api.system.zone.LevelZoneData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Beyond 模块 API 入口
 */
public class BeyondAPI {

    /**
     * 获取进度目录数据
     * 目前只有主世界有这个数据
     * @param level 世界
     * @return ProgressCatalog，非主世界返回 null
     */
    public static ProgressCatalog getProgressCatalog(Level level) {
        return BeyondAttachInit.getProgressCatalog(level);
    }

    /**
     * 获取区域数据
     * 目前只有主世界有这个数据
     * @param level 世界
     * @return LevelZoneData，非主世界返回 null
     */
    public static LevelZoneData getZoneData(Level level) {
        return BeyondAttachInit.getLevelZoneData(level);
    }

    /**
     * 获取进度管理器
     * 目前只有主世界有这个数据
     * @param level 世界
     * @return IProgressManager，非主世界返回 null
     */
    public static IProgressManager getProgressManager(Level level) {
        return BeyondAttachInit.getProgressManager(level);
    }

    /**
     * 获取进度管理器（返回具体类型）
     * 目前只有主世界有这个数据
     * @param level 世界
     * @return ProgressManager，非主世界返回 null
     */
    public static ProgressManager getProgressManagerImpl(Level level) {
        return BeyondAttachInit.getProgressManager(level);
    }

    public static BeyondData getBeyondData(Player player) {
        return BeyondAttachInit.getBeyondData(player);

    }
}
