package org.galaxylib.api.system.loot;

import lombok.Builder;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.item.ItemStack;
import org.galaxylib.api.GalaxyLibAPI;
import org.galaxylib.api.init.GalaxyLibLootTypeInit;
import org.galaxylib.api.system.loot.core.ILootType;
import org.galaxylib.api.system.loot.data.LootEntryDefinition;
import org.galaxylib.api.system.loot.data.LootPoolDefinition;
import org.galaxylib.api.system.loot.data.LootTableDefinition;
import org.galaxylib.api.system.random.RandomManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LootManager {

    private Map<ResourceLocation, LootTableDefinition> tables = Map.of();

    public void setTables(Map<ResourceLocation, LootTableDefinition> tables) {
        this.tables = tables == null ? Map.of() : Map.copyOf(tables);
    }

    public Map<ResourceLocation, LootTableDefinition> getTables() {
        return tables;
    }

    /**
     * 生成多个候选项。每个候选项都是一次完整 roll，适合三选一这种 UI。
     */
    public ChoiceResult rollChoices(Request request, Context context, int choiceCount) {
        List<Result<?>> options = new ArrayList<>();
        for (int i = 0; i < choiceCount; i++) {
            options.add(roll(request, context));
        }
        return new ChoiceResult(options);
    }

    /**
     * 执行一次抽取，并交给 LootType 把 typed values 打包成最终展示/领取用的 ItemStack。
     */
    public <T> Result<T> roll(Request request, Context context) {
        ILootType<T> type = (ILootType<T>) request.getLootType();
        List<LootTableDefinition> candidates = findTables(request);
        List<Value<T>> values = new ArrayList<>();
        Set<ResourceLocation> used = new HashSet<>();
        WeightModifier weightModifier = request.getWeightModifier() == null ? WeightModifier.IDENTITY : request.getWeightModifier();

        for (int i = 0; i < Math.max(1, request.getRolls()); i++) {
            rollOne(type, candidates, context, request.isReplacement(), weightModifier, used).ifPresent(values::add);
        }

        Bundle<T> bundle = new Bundle<>(type, values);
        ItemStack stack = type.createStack(bundle, context);
        return new Result<>(type, values, stack);
    }

    private List<LootTableDefinition> findTables(Request request) {
        ResourceLocation lootTypeId = GalaxyLibLootTypeInit.getLootTypeId(request.getLootType());
        if (!request.getTables().isEmpty()) {
            return request.getTables().stream()
                    .map(tables::get)
                    .filter(table -> table != null && table.lootType().equals(lootTypeId))
                    .toList();
        }

        return tables.values().stream()
                .filter(table -> table.lootType().equals(lootTypeId))
                .toList();
    }

    private <T> Optional<Value<T>> rollOne(
            ILootType<T> type,
            List<LootTableDefinition> candidates,
            Context context,
            boolean replacement,
            WeightModifier weightModifier,
            Set<ResourceLocation> used
    ) {
        for (int attempts = 0; attempts < 32; attempts++) {
            LootTableDefinition table = randomTable(candidates, context);
            if (table == null) return Optional.empty();

            WeightedRandomList<WeightedEntry.Wrapper<LootPoolDefinition>> pools = poolsAsWeightedList(table, context, weightModifier);
            LootPoolDefinition pool = pools.getRandom(context.random())
                    .map(WeightedEntry.Wrapper::data)
                    .orElse(null);
            if (pool == null) continue;

            WeightedRandomList<WeightedEntry.Wrapper<LootEntryDefinition>> entries = entriesAsWeightedList(table, pool, context, weightModifier);
            LootEntryDefinition entry = entries.getRandom(context.random())
                    .map(WeightedEntry.Wrapper::data)
                    .orElse(null);
            if (entry == null) continue;
            if (!replacement && used.contains(entry.id())) continue;

            Optional<T> value = type.resolve(entry, context);
            if (value.isEmpty()) continue;

            used.add(entry.id());
            return Optional.of(new Value<>(value.get(), entry, table.identify(), pool.name()));
        }

        return Optional.empty();
    }

    private WeightedRandomList<WeightedEntry.Wrapper<LootPoolDefinition>> poolsAsWeightedList(
            LootTableDefinition table,
            Context context,
            WeightModifier weightModifier
    ) {
        List<WeightedEntry.Wrapper<LootPoolDefinition>> pools = new ArrayList<>();
        for (LootPoolDefinition pool : table.pools()) {
            int weight = weightModifier.modifyPoolWeight(table, pool, context);
            if (weight > 0) {
                pools.add(WeightedEntry.wrap(pool, weight));
            }
        }
        return WeightedRandomList.create(pools);
    }

    private WeightedRandomList<WeightedEntry.Wrapper<LootEntryDefinition>> entriesAsWeightedList(
            LootTableDefinition table,
            LootPoolDefinition pool,
            Context context,
            WeightModifier weightModifier
    ) {
        List<WeightedEntry.Wrapper<LootEntryDefinition>> entries = new ArrayList<>();
        for (LootEntryDefinition entry : pool.entries()) {
            int weight = weightModifier.modifyEntryWeight(table, pool, entry, context);
            if (weight > 0) {
                entries.add(WeightedEntry.wrap(entry, weight));
            }
        }
        return WeightedRandomList.create(entries);
    }

    private LootTableDefinition randomTable(List<LootTableDefinition> candidates, Context context) {
        if (candidates.isEmpty()) return null;
        int index = context.random().nextInt(candidates.size());
        return candidates.get(index);
    }

    /**
     * 对外抽取请求。tables 为空时，会抽取所有匹配 lootType 的表。
     */
    @Getter
    @Builder
    public static class Request {
        private final ILootType<?> lootType;
        @Builder.Default
        private final List<ResourceLocation> tables = List.of();
        @Builder.Default
        private final int rolls = 1;
        @Builder.Default
        private final boolean replacement = true;
        @Builder.Default
        private final String randomId = RandomManager.PROGRESS_RANDOM_ID;
        @Builder.Default
        private final WeightModifier weightModifier = WeightModifier.IDENTITY;
    }

    public interface WeightModifier {

        WeightModifier IDENTITY = new WeightModifier() {
        };

        default int modifyPoolWeight(LootTableDefinition table, LootPoolDefinition pool, Context context) {
            return pool.baseWeight();
        }

        default int modifyEntryWeight(LootTableDefinition table, LootPoolDefinition pool, LootEntryDefinition entry, Context context) {
            return entry.weight();
        }
    }

    public record Context(ServerPlayer player, ServerLevel level, RandomSource random) {

        public static Context of(ServerPlayer player) {
            return of(player, RandomManager.PROGRESS_RANDOM_ID);
        }

        public static Context of(ServerPlayer player, String randomId) {
            ServerLevel level = player.serverLevel();
            RandomSource random = GalaxyLibAPI.getRandomManager(level).getSeed(randomId);
            return new Context(player, level, random);
        }
    }

    public record ChoiceResult(List<Result<?>> options) {
    }

    public record Result<T>(ILootType<T> lootType, List<Value<T>> values, ItemStack stack) {
    }

    public record Bundle<T>(ILootType<T> type, List<Value<T>> values) {

        public Optional<Value<T>> first() {
            return values.isEmpty() ? Optional.empty() : Optional.of(values.getFirst());
        }
    }

    public record Value<T>(T value, LootEntryDefinition entry, ResourceLocation tableId, String poolName) {
    }

    public record ClaimResult(boolean success, ItemStack stack) {

        public static ClaimResult success(ItemStack stack) {
            return new ClaimResult(true, stack.copy());
        }

        public static ClaimResult empty() {
            return new ClaimResult(false, ItemStack.EMPTY);
        }
    }
}
