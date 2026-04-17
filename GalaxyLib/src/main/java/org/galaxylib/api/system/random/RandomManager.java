package org.galaxylib.api.system.random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 随机源管理器，附加在 Level 上
 */
public class RandomManager {

    public static final String SEED_MAP = "seed_map";

    /**
     * Progress 进度系统的统一随机源 ID
     */
    public static final String PROGRESS_RANDOM_ID = "progress";

    private static final Codec<Map<String, Long>> SEED_MAP_CODEC = Codec.unboundedMap(Codec.STRING, Codec.LONG);

    private static final StreamCodec<RegistryFriendlyByteBuf, List<Map.Entry<String, Long>>> ENTRY_LIST_STREAM_CODEC =
            ByteBufCodecs.collection(ArrayList::new, StreamCodec.of(
                    (buf, entry) -> {
                        buf.writeUtf(entry.getKey());
                        buf.writeLong(entry.getValue());
                    },
                    (buf) -> Map.entry(buf.readUtf(), buf.readLong())
            ));

    public static final Codec<RandomManager> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            SEED_MAP_CODEC.optionalFieldOf(SEED_MAP, Map.of()).forGetter(RandomManager::getSeedMapForCodec)
    ).apply(builder, RandomManager::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RandomManager> STREAM_CODEC = StreamCodec.of(
            (buf, manager) -> {
                List<Map.Entry<String, Long>> entries = new ArrayList<>(manager.seedMap.entrySet());
                ENTRY_LIST_STREAM_CODEC.encode(buf, entries);
            },
            (buf) -> {
                List<Map.Entry<String, Long>> entries = ENTRY_LIST_STREAM_CODEC.decode(buf);
                Map<String, Long> map = new HashMap<>();
                for (Map.Entry<String, Long> entry : entries) {
                    map.put(entry.getKey(), entry.getValue());
                }
                return new RandomManager(map);
            }
    );

    /**
     * 种子存储（可序列化）
     */
    private Map<String, Long> seedMap = new HashMap<>();
    
    /**
     * 随机源缓存（运行时，不序列化）
     */
    private transient Map<String, SingleThreadedRandomSource> randomMap = new HashMap<>();

    public RandomManager() {
    }

    public RandomManager(Map<String, Long> seedMap) {
        this.seedMap = seedMap != null ? new HashMap<>(seedMap) : new HashMap<>();
    }

    private Map<String, Long> getSeedMapForCodec() {
        return seedMap;
    }

    public void createRandom(String modId) {
        long seed = RandomSupport.generateUniqueSeed();
        seedMap.put(modId, seed);
        randomMap.put(modId, new SingleThreadedRandomSource(seed));
    }

    public SingleThreadedRandomSource getSeed(String modId) {
        if (!randomMap.containsKey(modId)) {
            Long seed = seedMap.get(modId);
            if (seed != null) {
                randomMap.put(modId, new SingleThreadedRandomSource(seed));
            } else {
                createRandom(modId);
            }
        }
        return randomMap.get(modId);
    }

    /**
     * 刷新指定 modId 的随机源（使用新种子）
     */
    public void refresh(String modId) {
        long seed = RandomSupport.generateUniqueSeed();
        seedMap.put(modId, seed);
        randomMap.put(modId, new SingleThreadedRandomSource(seed));
    }
}
