package org.galaxy.beyond.api.system.random;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.HashMap;
import java.util.Map;

public class RogueRandom {

    // 关卡指定random源，让固定种子有相同的游戏体验
    public static final String PROGRESS = "progress";

    // 整局游戏的种子，开局时从进度种子派生，一局内不变
    public static final String GAME = "game";

    // 刷怪和生成节点颜色专用random源
    public static final String ENCOUNTER = "encounter";

    @Getter @Setter
    private Map<String, SingleThreadedRandomSource> randoms = new HashMap<>();
    private final Map<String, Long> seeds = new HashMap<>();

    public RogueRandom() {}

    /**
     * 仅限关卡初始化、关卡event生成使用，保证相同体验
     */
    public SingleThreadedRandomSource getProgressRandom() {
        return getRandom(PROGRESS);
    }

    /**
     * 整局游戏随机源，开局时从进度种子克隆，一局不变
     */
    public SingleThreadedRandomSource getGameRandom() {
        return getRandom(GAME);
    }

    /**
     * 大部分时间用这个
     */
    public SingleThreadedRandomSource getEncounterRandom() {
        return getRandom(ENCOUNTER);
    }

    /**
     * 开局时调用：从当前进度种子克隆出本局种子，固化整局体验
     */
    public long deriveGameSeed() {
        long progressSeed = getProgressSeed();
        seeds.put(GAME, progressSeed);
        SingleThreadedRandomSource gameRandom = randoms.get(GAME);
        if (gameRandom != null) {
            gameRandom.setSeed(progressSeed);
        } else {
            randoms.put(GAME, new SingleThreadedRandomSource(progressSeed));
        }
        return progressSeed;
    }

    public SingleThreadedRandomSource resetProgressSeed() {
        return resetSeed(PROGRESS);
    }

    public SingleThreadedRandomSource resetEncounterSeed() {
        return resetSeed(ENCOUNTER);
    }

    public long getProgressSeed() {
        return getSeed(PROGRESS);
    }

    public long getGameSeed() {
        return getSeed(GAME);
    }

    public long getEncounterSeed() {
        return getSeed(ENCOUNTER);
    }

    public SingleThreadedRandomSource getRandom(String name) {
        return randoms.computeIfAbsent(name, k -> {
            long seed = RandomSupport.generateUniqueSeed();
            seeds.put(k, seed);
            return new SingleThreadedRandomSource(seed);
        });
    }

    public SingleThreadedRandomSource resetSeed(String name) {
        long seed = RandomSupport.generateUniqueSeed();
        seeds.put(name, seed);
        getRandom(name).setSeed(seed);
        return getRandom(name);
    }

    public long getSeed(String name) {
        getRandom(name);
        return seeds.get(name);
    }
}
