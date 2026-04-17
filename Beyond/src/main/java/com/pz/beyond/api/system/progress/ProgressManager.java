package com.pz.beyond.api.system.progress;

import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.system.progress.core.IProgressManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Consumer;

/**
 * 进度管理器，挂载在 Level Attachment 上，包含控制逻辑
 */
public class ProgressManager implements IProgressManager {

    private ServerLevel level;

    /**
     * 无参构造，用于 Attachment 初始化
     */
    public ProgressManager() {
    }

    public ProgressManager(ServerLevel level) {
        this.level = level;
    }

    /**
     * 获取当前 Level 的 ProgressCatalog 数据
     */
    private ProgressCatalog getCatalog() {
        if (level == null) {
            return null;
        }
        return BeyondAttachInit.getProgressCatalog(level);
    }

    /**
     * 获取 ProgressCatalog 数据（公开接口）
     */
    public ProgressCatalog getCatalogPublic() {
        return getCatalog();
    }

    @Override
    public void setCurrentProgress(ResourceLocation id) {
        ProgressCatalog catalog = getCatalog();
        if (catalog != null) {
            catalog.setCurrentProgressById(id);
        }
    }

    @Override
    public void createProgress(ResourceLocation id, Consumer<ProgressType.Builder> consumer) {
        ProgressCatalog catalog = getCatalog();
        if (catalog != null) {
            ProgressType progressType = ProgressType.create(id, consumer);
            catalog.addProgress(Progress.create(progressType));
        }
    }

    @Override
    public void startGame() {
        ProgressCatalog catalog = getCatalog();
        if (catalog != null) {
            catalog.startGame();
        }
    }
}
