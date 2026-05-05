package org.galaxy.beyond.api.system.random;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.HashMap;
import java.util.Map;

public class RogueRandom {

    //关卡指定random源，让固定种子有相同的游戏体验。特别是在指定节点，奖励和事件会相同。
    public static final String PROGRESS = "progress";

    //刷怪和生成节点颜色专用random源。
    public static final String ENCOUNTER = "encounter";

    @Getter @Setter
    private Map<String, SingleThreadedRandomSource> randoms = new HashMap<>();
    private final Map<String, Long> seeds = new HashMap<>();

    public RogueRandom() {}

    /**
     * 仅限关卡初始化，还有关卡event 生成 能够使用。保证相同体验
     * @return
     */
    public SingleThreadedRandomSource getProgressRandom(){
        return getRandom(PROGRESS);
    }

    /**
     * 大部分时间用这个
     * @return
     */
    public SingleThreadedRandomSource getEncounterRandom(){
        return getRandom(ENCOUNTER);
    }

    public SingleThreadedRandomSource resetProgressSeed(){
        return resetSeed(PROGRESS);
    }
    public SingleThreadedRandomSource resetEncounterSeed(){
        return resetSeed(ENCOUNTER);
    }
    public long getProgressSeed(){
        return getSeed(PROGRESS);
    }
    public long getEncounterSeed(){
        return getSeed(ENCOUNTER);
    }

    public SingleThreadedRandomSource getRandom(String name){
        return randoms.computeIfAbsent(name, k -> {
            long seed = RandomSupport.generateUniqueSeed();
            seeds.put(k, seed);
            return new SingleThreadedRandomSource(seed);
        });
    }

    public SingleThreadedRandomSource resetSeed(String name){
        long seed = RandomSupport.generateUniqueSeed();
        seeds.put(name, seed);
        getRandom(name).setSeed(seed);
        return getRandom(name);
    }

    public long getSeed(String name){
        getRandom(name);
        return seeds.get(name);
    }
}
