package org.galaxy.beyond.api.init;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.rogue.phase.node.*;
import org.galaxy.beyond.api.system.rogue.phase.player.*;
import org.galaxy.beyond.api.system.rogue.phase.rogue.*;

import java.util.function.Supplier;

/**
 * 三种 Phase 注册 —— 玩家 / 节点 / 肉鸽。
 * <pre>
 *   // 获取 Phase 实例
 *   BeyondPhaseInit.PLAYER_LOBBY.get()
 *   BeyondPhaseInit.get(BeyondPhaseInit.PLAYER_LOBBY)
 *
 *   // 按 Identifier 查找
 *   BeyondPhaseInit.getPlayerPhase(BeyondPhaseInit.P_LOBBY)
 *
 *   // 覆盖注册自定义 Phase
 *   BeyondPhaseInit.PLAYER_LOBBY = registerPlayer(P_LOBBY, MyLobbyPhase::new)
 * </pre>
 */
public class BeyondPhaseInit {

    public static void register(IEventBus eventBus) {}

    // ============================================================
    // Player Phase — Identifier 常量 + Supplier<PlayerPhase>
    // ============================================================

    public static final Identifier P_LOBBY          = id("player/lobby");
    public static final Identifier P_PRE_ROGUE      = id("player/pre_rogue");
    public static final Identifier P_ON_PROGRESS    = id("player/on_progress");
    public static final Identifier P_PRE_NODE       = id("player/pre_node");
    public static final Identifier P_PRE_EVENT      = id("player/pre_event");
    public static final Identifier P_ON_EVENT       = id("player/on_event");
    public static final Identifier P_SPECTATOR      = id("player/spectator");
    public static final Identifier P_DEAD           = id("player/dead");
    public static final Identifier P_REWARD         = id("player/reward");
    public static final Identifier P_PROGRESS_FINISH = id("player/progress_finish");

    public static final Supplier<PlayerPhase> PLAYER_LOBBY         = registerPlayer(P_LOBBY,          PlayerLobbyPhase::new);
    public static final Supplier<PlayerPhase> PLAYER_PRE_ROGUE     = registerPlayer(P_PRE_ROGUE,      PlayerPreRoguePhase::new);
    public static final Supplier<PlayerPhase> PLAYER_ON_PROGRESS   = registerPlayer(P_ON_PROGRESS,    PlayerOnProgressPhase::new);
    public static final Supplier<PlayerPhase> PLAYER_PRE_NODE      = registerPlayer(P_PRE_NODE,       PlayerPreNodePhase::new);
    public static final Supplier<PlayerPhase> PLAYER_PRE_EVENT     = registerPlayer(P_PRE_EVENT,      PlayerPreEventPhase::new);
    public static final Supplier<PlayerPhase> PLAYER_ON_EVENT      = registerPlayer(P_ON_EVENT,       PlayerOnEventPhase::new);
    public static final Supplier<PlayerPhase> PLAYER_SPECTATOR     = registerPlayer(P_SPECTATOR,      PlayerSpectatorPhase::new);
    public static final Supplier<PlayerPhase> PLAYER_DEAD          = registerPlayer(P_DEAD,           PlayerDeadPhase::new);
    public static final Supplier<PlayerPhase> PLAYER_REWARD        = registerPlayer(P_REWARD,         PlayerRewardPhase::new);
    public static final Supplier<PlayerPhase> PLAYER_PROGRESS_FINISH = registerPlayer(P_PROGRESS_FINISH, PlayerProgressFinishPhase::new);

    public static PlayerPhase getPlayerPhase(Identifier id) {
        return BeyondRegistries.PLAYER_PHASE.get(id).map(h -> h.value()).orElse(null);
    }

    // ============================================================
    // Node Phase
    // ============================================================

    public static final Identifier N_LOCKED     = id("node/locked");
    public static final Identifier N_PRE_NODE   = id("node/pre_node");
    public static final Identifier N_PRE_EVENT  = id("node/pre_event");
    public static final Identifier N_ON_EVENT   = id("node/on_event");
    public static final Identifier N_UNLOCKED   = id("node/unlocked");

    public static final Supplier<NodePhase> NODE_LOCKED     = registerNode(N_LOCKED,     NodeLockedPhase::new);
    public static final Supplier<NodePhase> NODE_PRE_NODE   = registerNode(N_PRE_NODE,   NodePreNodePhase::new);
    public static final Supplier<NodePhase> NODE_PRE_EVENT  = registerNode(N_PRE_EVENT,  NodePreEventPhase::new);
    public static final Supplier<NodePhase> NODE_ON_EVENT   = registerNode(N_ON_EVENT,   NodeOnEventPhase::new);
    public static final Supplier<NodePhase> NODE_UNLOCKED   = registerNode(N_UNLOCKED,   NodeUnlockedPhase::new);

    public static NodePhase getNodePhase(Identifier id) {
        return BeyondRegistries.NODE_PHASE.get(id).map(h -> h.value()).orElse(null);
    }

    // ============================================================
    // Rogue Phase
    // ============================================================

    public static final Identifier R_LOBBY        = id("rogue/lobby");
    public static final Identifier R_PRE_ROGUE    = id("rogue/pre_rogue");
    public static final Identifier R_INIT         = id("rogue/rogue_init");
    public static final Identifier R_ON_PROGRESS  = id("rogue/on_progress");
    public static final Identifier R_PROGRESS_FINISH = id("rogue/rogue_progress_finish");

    public static final Supplier<RoguePhase> ROGUE_LOBBY         = registerRogue(R_LOBBY,         RogueLobbyPhase::new);
    public static final Supplier<RoguePhase> ROGUE_PRE_ROGUE     = registerRogue(R_PRE_ROGUE,     RoguePreRoguePhase::new);
    public static final Supplier<RoguePhase> ROGUE_INIT          = registerRogue(R_INIT,          RogueInitPhase::new);
    public static final Supplier<RoguePhase> ROGUE_ON_PROGRESS   = registerRogue(R_ON_PROGRESS,   RogueOnProgressPhase::new);
    public static final Supplier<RoguePhase> ROGUE_PROGRESS_FINISH = registerRogue(R_PROGRESS_FINISH, RogueProgressFinishPhase::new);

    public static RoguePhase getRoguePhase(Identifier id) {
        return BeyondRegistries.ROGUE_PHASE.get(id).map(h -> h.value()).orElse(null);
    }

    // ============================================================
    // internal
    // ============================================================

    private static Identifier id(String path) {
        return Identifier.parse(Beyond.MODID + ":" + path);
    }

    private static Supplier<PlayerPhase> registerPlayer(Identifier id, Supplier<PlayerPhase> sup) {
        return BeyondRegistries.PLAYER_PHASE_REGISTER.register(
                id.getPath().substring(id.getPath().lastIndexOf('/') + 1), sup);
    }

    private static Supplier<NodePhase> registerNode(Identifier id, Supplier<NodePhase> sup) {
        return BeyondRegistries.NODE_PHASE_REGISTER.register(
                id.getPath().substring(id.getPath().lastIndexOf('/') + 1), sup);
    }

    private static Supplier<RoguePhase> registerRogue(Identifier id, Supplier<RoguePhase> sup) {
        return BeyondRegistries.ROGUE_PHASE_REGISTER.register(
                id.getPath().substring(id.getPath().lastIndexOf('/') + 1), sup);
    }
}
