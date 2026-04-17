package org.galaxylib.api.system.loot.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.galaxylib.api.init.GalaxyLibLootTypeInit;
import org.galaxylib.api.system.loot.data.LootPoolData;
import org.galaxylib.api.system.loot.data.LootTableGroupBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 战利品表管理器接口
 */
public interface ILootTableManager {

    /**
     * 修改战利品表，支持链式调用
     * @param lootType 战利品类型
     * @param builder 消费 Builder 进行链式修改
     * @return 返回自身以支持链式调用
     */
    ILootTableManager modify(Supplier<ILootType<?>> lootType, Consumer<LootTableGroupBuilder> builder);

    /**
     * 不放回抽取
     * <p>
     * 每次抽取后将物品从池子移除，同一物品只能被抽中一次
     *
     * @param lootType 战利品类型
     * @return 抽取结果列表
     */
    LootResult roolWithoutReplacement(Supplier<ILootType<?>> lootType);
    LootResult roolWithoutReplacement(ILootType<?> lootType);

    /**
     * 不放回抽取（指定随机源）
     */
    default LootResult roolWithoutReplacement(ILootType<?> lootType, String randomId) {
        return roolWithoutReplacement(lootType);
    }

    /**
     * 放回抽取
     * <p>
     * 每次抽取后将物品放回池子，同一物品可被多次抽中
     *
     * @param lootType 战利品类型
     * @return 抽取结果列表
     */
    LootResult rollWithReplacement(Supplier<ILootType<?>> lootType);
    LootResult rollWithReplacement(ILootType<?> lootType);

    /**
     * 放回抽取（指定随机源）
     */
    default LootResult rollWithReplacement(ILootType<?> lootType, String randomId) {
        return rollWithReplacement(lootType);
    }
    /**
     * 批量设置指定名称的所有 pool 的 weight
     * <p>
     * 允许重复名字，所有匹配的 pool 都会被修改
     *
     * @param name pool 名称
     * @param weight 新权重
     */
    void setWeightByName(String name, int weight);

    /**
     * 将多个 lootTable 合并在一起
     * <p>
     * 将指定战利品表的所有条目合并到目标战利品表中
     *
     * @param targetLootType    目标战利品类型
     * @param lootTableIdentify 要合并的战利品表 ID
     */
    void merge(Supplier<ILootType<?>> targetLootType, ResourceLocation... lootTableIdentify);

    /**
     * 领取抽取结果，将战利品发放给玩家
     * @param results 抽取结果列表
     */
    void claimResults(LootResult results);

    /**
     * 抽取结果记录
     */
    record LootResult(List<LootPoolData.Entry> result, ILootType<?> lootType) {

        /**
         * 获取战利品类型的 ID（用于序列化）
         */
        private Optional<ResourceLocation> getLootTypeId() {
            return Optional.ofNullable(lootType)
                .map(type -> GalaxyLibLootTypeInit.LOOT_TYPE_REGISTRY.getKey(type));
        }

        /**
         * 根据 ID 解析战利品类型
         */
        private static ILootType<?> resolveLootType(Optional<ResourceLocation> lootTypeId) {
            return lootTypeId.map(GalaxyLibLootTypeInit::getLootTypeById).orElse(null);
        }

        public static final Codec<LootResult> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                LootPoolData.Entry.CODEC.listOf().fieldOf("result").forGetter(LootResult::result),
                ResourceLocation.CODEC.optionalFieldOf("loot_type_id").forGetter(LootResult::getLootTypeId)
            ).apply(instance, (entries, lootTypeId) -> new LootResult(entries, resolveLootType(lootTypeId)))
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, LootResult> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, LootPoolData.Entry.STREAM_CODEC),
            LootResult::result,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            LootResult::getLootTypeId,
            (entries, lootTypeId) -> new LootResult(entries, resolveLootType(lootTypeId))
        );
    }

    void init();
}
