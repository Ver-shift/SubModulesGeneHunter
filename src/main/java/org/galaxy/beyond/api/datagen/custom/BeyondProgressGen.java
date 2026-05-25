package org.galaxy.beyond.api.datagen.custom;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import org.galaxy.beyond.data.progress.ForestProgress;
import org.galaxy.beyond.data.progress.TutorialProgress;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Beyond 关卡数据生成器 —— 在此注册所有关卡。
 * <p>
 * 添加新关卡：在 {@link #registerProgress} 中加一行 entries.add(...)。
 */
public class BeyondProgressGen extends RogueProgressProvider {

    public BeyondProgressGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void registerProgress(List<Entry> entries) {
        entries.add(new Entry("tutorial", new TutorialProgress().build()));
        entries.add(new Entry("forest",    new ForestProgress().build()));
    }

    @Override
    public String getName() {
        return "Beyond Rogue Progress";
    }
}
