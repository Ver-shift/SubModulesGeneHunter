package com.pz.beyond.commend;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.system.BeyondLevelData;
import com.pz.beyond.api.system.definition.ProgressDefinition;
import com.pz.beyond.api.system.progress.ProgressManager;
import com.pz.beyond.api.system.node.StructureKey;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Map;

/**
 * Beyond 指令注册入口。
 * <p>
 * 当前提供：
 * <ul>
 *     <li>{@code /beyond home <player>} —— 将目标玩家直接传送回其出生点
 *     （{@link ServerPlayer#getRespawnPosition()}），由项目其他逻辑手动维护该字段。</li>
 *     <li>{@code /beyond progress <id>} —— 切换当前关卡，<id> 从主世界
 *     {@link BeyondLevelData#getProgressDefinitions()} 动态补全。</li>
 *     <li>{@code /beyond reset-node} —— 重置当前激活节点为 LOCKED。</li>
 * </ul>
 */
@EventBusSubscriber(modid = Beyond.MODID)
public class BeyondCommand {

    private BeyondCommand() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("beyond")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("home")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(BeyondCommand::runHome)
                                )
                        )
                        .then(Commands.literal("progress")
                                .then(Commands.argument("id", ResourceLocationArgument.id())
                                        .suggests(PROGRESS_ID_SUGGESTIONS)
                                        .executes(BeyondCommand::runProgress)
                                )
                        )
                        .then(Commands.literal("reset-node")
                                .executes(BeyondCommand::runResetNode)
                        )
        );
    }

    // ==================== /beyond progress <id> ====================

    /**
     * 关卡 id 补全：从主世界 BeyondLevelData 的 progressDefinitions 动态提取 key。
     * <p>progressDefinitions 由数据包 {@code ProgressPack} 加载后写入，
     * 因此数据包重载后会自动按最新定义集给出提示。</p>
     */
    private static final SuggestionProvider<CommandSourceStack> PROGRESS_ID_SUGGESTIONS = (ctx, builder) -> {
        MinecraftServer server = ctx.getSource().getServer();
        if (server == null) {
            return builder.buildFuture();
        }
        ServerLevel overworld = server.overworld();
        BeyondLevelData data = BeyondAPI.getBeyondLevelData(overworld);
        if (data == null || data.getProgressDefinitions() == null) {
            return builder.buildFuture();
        }
        Map<ResourceLocation, ProgressDefinition> defs = data.getProgressDefinitions();
        return SharedSuggestionProvider.suggestResource(defs.keySet().stream(), builder);
    };

    private static int runProgress(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
        MinecraftServer server = ctx.getSource().getServer();
        ServerLevel overworld = server.overworld();

        BeyondLevelData data = BeyondAPI.getBeyondLevelData(overworld);
        if (data == null || !data.getProgressDefinitions().containsKey(id)) {
            ctx.getSource().sendFailure(Component.translatable(
                    "commands.beyond.progress.not_found", id.toString()));
            return 0;
        }

        // 委托给 ProgressManager 统一切换（内部走 ITranslate 重置 ProgressData）
        ProgressManager progressManager = BeyondAPI.getBeyondManager().getProgressManager();
        progressManager.initProgress(id);

        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.beyond.progress.success", id.toString()), true);
        return 1;
    }

    // ==================== /beyond reset-node ====================

    private static int runResetNode(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        MinecraftServer server = ctx.getSource().getServer();
        if (server == null) {
            return 0;
        }
        ServerLevel overworld = server.overworld();
        ProgressManager progressManager = BeyondAPI.getBeyondManager().getProgressManager();
        StructureKey key = progressManager.resetActiveNode(overworld);
        if (key == null) {
            ctx.getSource().sendFailure(Component.translatable("commands.beyond.reset_node.none"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.beyond.reset_node.success", key.toString()), true);
        return 1;
    }

    // ==================== /beyond home <player> ====================

    private static int runHome(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        MinecraftServer server = ctx.getSource().getServer();

        // 出生点维度：玩家若未设置则回落到主世界
        ResourceKey<Level> dimKey = target.getRespawnDimension();
        ServerLevel dest = dimKey == null ? server.overworld() : server.getLevel(dimKey);
        if (dest == null) {
            dest = server.overworld();
        }

        // 出生点坐标：玩家若未设置则回落到该维度共享出生点
        BlockPos pos = target.getRespawnPosition();
        if (pos == null) {
            pos = dest.getSharedSpawnPos();
        }

        target.teleportTo(dest, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);

        BlockPos finalPos = pos;
        ServerLevel finalDest = dest;
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.beyond.home.success",
                target.getName().getString(),
                finalPos.getX(), finalPos.getY(), finalPos.getZ(),
                finalDest.dimension().location().toString()
        ), false);
        return 1;
    }
}
