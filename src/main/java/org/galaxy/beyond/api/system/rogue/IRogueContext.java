package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;

import java.util.List;
import java.util.function.Supplier;

/**
 * Rogue 运行时上下文 —— 统一入口，屏蔽底层数据存取细节。
 * <p>
 * 所有状态读写（phase、seed、玩家列表等）均通过此接口操作，
 * 不直接触碰 {@link RogueData} / {@link org.galaxy.beyond.api.system.BeyondPlayerData}。
 * <p>
 * 实现类 {@link RogueContext} 为纯委托，无内部状态，可安全复用。
 */
public interface IRogueContext {

    // ============================================================
    // 全局 RoguePhase
    // ============================================================

    /** 获取当前维度的全局 RoguePhase */
    RoguePhase getPhase(ServerLevel level);

    /** 设置当前维度的全局 RoguePhase */
    void setPhase(ServerLevel level, RoguePhase phase);

    /** 通过 Supplier 延迟创建并设置全局 RoguePhase */
    default void setPhase(ServerLevel level, Supplier<RoguePhase> supplier) {
        setPhase(level, supplier.get());
    }

    // ============================================================
    // 玩家 PlayerPhase
    // ============================================================

    /** 获取指定玩家的 PlayerPhase */
    PlayerPhase getPlayerPhase(ServerPlayer player);

    /** 设置指定玩家的 PlayerPhase */
    void setPlayerPhase(ServerPlayer player, PlayerPhase phase);

    /** 通过 Supplier 延迟创建并设置玩家 PlayerPhase */
    default void setPlayerPhase(ServerPlayer player, Supplier<PlayerPhase> supplier) {
        setPlayerPhase(player, supplier.get());
    }

    /** 将维度内所有肉鸽玩家的 PlayerPhase 设为相同值 */
    void setAllPlayerPhase(ServerLevel level, PlayerPhase phase);

    /** 通过 Supplier 延迟创建并批量设置所有肉鸽玩家 PlayerPhase */
    default void setAllPlayerPhase(ServerLevel level, Supplier<PlayerPhase> supplier) {
        setAllPlayerPhase(level, supplier.get());
    }

    /** 检查维度内所有肉鸽玩家是否处于同一 PlayerPhase */
    boolean allPlayersMatchPhase(ServerLevel level, PlayerPhase phase);

    // ============================================================
    // 玩家查询
    // ============================================================

    /** 获取维度内当前在肉鸽列表中的所有在线玩家 */
    List<ServerPlayer> playersInRogue(ServerLevel level);

    // ============================================================
    // 数据访问
    // ============================================================

    /** 获取维度的 RogueData */
    RogueData getRogueData(ServerLevel level);

    /** 获取维度的 RogueNodeData（当前激活节点），可能为 null */
    RogueNodeData getRogueNodeData(ServerLevel level);

    // ============================================================
    // 游戏种子
    // ============================================================

    /** 获取当前局游戏种子 */
    long getGameSeed(ServerLevel level);

    /** 设置当前局游戏种子 */
    void setGameSeed(ServerLevel level, long seed);
}
