package com.pz.beyond.api;

import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.system.progress.ProgressCatalog;
import com.pz.beyond.api.system.zone.LevelZoneData;
import com.pz.beyond.api.system.zone.ZoneData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class BeyondAPI {

    /**
     * 目前只有主世界有这个数据哦。
     * @param level
     * @return
     */
    public static ProgressCatalog getProgressCatalog(Level level) {
        return level.getData(BeyondAttachInit.PROGRESS_CATALOG);
    }

    public static LevelZoneData getZoneData(Level level) {
        return level.getData(BeyondAttachInit.LEVEL_ZONE_DATA);
    }

}
