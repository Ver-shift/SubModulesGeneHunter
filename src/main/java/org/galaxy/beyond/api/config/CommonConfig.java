package org.galaxy.beyond.api.config;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jspecify.annotations.NonNull;

/**
 * 通用模组配置文件（COMMON 类型，客户端和服务端均生效）。
 */
public class CommonConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 是否输出 Beyond 模块的调试信息 */
    public static final ModConfigSpec.BooleanValue DEBUG_MODE;

    /** 肉鸽玩法生效的维度，格式为 "namespace:path"，如 "minecraft:overworld" */
    private static final ModConfigSpec.ConfigValue<String> ROGUE_DIMENSION;

    /** 安全区向外最小拓展距离（区块数） */
    public static final ModConfigSpec.IntValue ACTIVE_ZONE_MIN_EXPAND;
    /** 安全区向外最大拓展距离（区块数），超过仍未满足条件则以当前范围注册 */
    public static final ModConfigSpec.IntValue ACTIVE_ZONE_MAX_EXPAND;
    /** 初始活动区至少需要覆盖的节点数量 */
    public static final ModConfigSpec.IntValue ACTIVE_ZONE_MIN_NODES;
    /** 节点完成后向外拓展的初始半径（区块数） */
    public static final ModConfigSpec.IntValue ACTIVE_ZONE_NODE_EXPAND_RADIUS;
    /** 节点完成后向外拓展时允许扫描的最大半径（区块数） */
    public static final ModConfigSpec.IntValue ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS;
    /** 节点拓展后至少需要扫描到的其他未完成节点连接数 */
    public static final ModConfigSpec.IntValue ACTIVE_ZONE_MIN_CONNECTIONS;

    /** 节点颜色权重：绿色 */
    public static final ModConfigSpec.IntValue NODE_COLOR_GREEN_WEIGHT;
    /** 节点颜色权重：橙色 */
    public static final ModConfigSpec.IntValue NODE_COLOR_ORANGE_WEIGHT;
    /** 节点颜色权重：红色 */
    public static final ModConfigSpec.IntValue NODE_COLOR_RED_WEIGHT;

    /** 玩家离开安全区的最小冷却时间（秒），防止频繁出入触发游戏入口流程 */
    public static final ModConfigSpec.IntValue LOBBY_COOLDOWN_SECONDS;

    static {
        BUILDER.comment("Beyond 通用调试开关").push("generic");
        DEBUG_MODE = BUILDER
                .comment("是否输出 Beyond 模块的调试日志。关闭后 debugInfo() 将被静默。")
                .define("debugMessages", false);
        BUILDER.pop();

        BUILDER.comment("肉鸽玩法维度配置").push("rogue");
        ROGUE_DIMENSION = BUILDER
                .comment("肉鸽玩法生效的维度 ID，格式为 \"namespace:path\"")
                .define("dimension", "minecraft:overworld");
        BUILDER.pop();

        BUILDER.comment("活动区域配置").push("active_zone");
        ACTIVE_ZONE_MIN_EXPAND = BUILDER
                .comment("安全区向外最小拓展距离（区块数），确保初始活动区有足够的空间。")
                .defineInRange("minExpand", 50, 1, 500);
        ACTIVE_ZONE_MAX_EXPAND = BUILDER
                .comment("安全区向外最大拓展距离（区块数），超过仍未满足节点数量则以当前范围注册活动区。")
                .defineInRange("maxExpand", 400, 1, 1000);
        ACTIVE_ZONE_MIN_NODES = BUILDER
                .comment("初始活动区至少需要覆盖的未完成节点数量。")
                .defineInRange("minNodes", 12, 1, 1000);
        ACTIVE_ZONE_NODE_EXPAND_RADIUS = BUILDER
                .comment("玩家完成节点事件后，以节点每个区块为中心向外扩张的半径（区块数）。")
                .defineInRange("nodeExpandRadius", 6, 1, 100);
        ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS = BUILDER
                .comment("玩家完成节点事件后，为寻找足够未完成节点允许扫描的最大半径（区块数）。")
                .defineInRange("nodeExpandMaxRadius", 400, 1, 1000);
        ACTIVE_ZONE_MIN_CONNECTIONS = BUILDER
                .comment("节点扩张后至少需要扫描到的其他未完成节点连接数，防止玩家卡关。")
                .defineInRange("minConnections", 3, 1, 100);
        BUILDER.pop();

        BUILDER.comment("节点颜色权重配置").push("node_color");
        NODE_COLOR_GREEN_WEIGHT = BUILDER
                .comment("绿色节点的生成权重（默认 20）")
                .defineInRange("greenWeight", 20, 0, 1000);
        NODE_COLOR_ORANGE_WEIGHT = BUILDER
                .comment("橙色节点的生成权重（默认 60）")
                .defineInRange("orangeWeight", 60, 0, 1000);
        NODE_COLOR_RED_WEIGHT = BUILDER
                .comment("红色节点的生成权重（默认 20）")
                .defineInRange("redWeight", 20, 0, 1000);
        BUILDER.pop();

        BUILDER.comment("大厅/准备阶段配置").push("lobby");
        LOBBY_COOLDOWN_SECONDS = BUILDER
                .comment("玩家离开安全区的最小冷却时间（秒），防止频繁出入触发游戏入口流程。")
                .defineInRange("cooldownSeconds", 10, 0, 3600);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    /**
     * 获取配置中指定的肉鸽玩法维度。
     */
    public static ResourceKey<Level> getRogueDimension() {
        return ResourceKey.create(Registries.DIMENSION, Identifier.parse(ROGUE_DIMENSION.get()));
    }
    private CommonConfig() {
    }
}
