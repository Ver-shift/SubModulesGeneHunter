package org.galaxy.beyond.api.init;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import org.galaxy.beyond.api.system.rogue.cap.NodeCap;
import org.galaxy.beyond.api.system.rogue.cap.NodeZoneEnterCap;
import org.galaxy.beyond.api.system.rogue.cap.PlayerInGameCap;
import org.galaxy.beyond.api.system.rogue.cap.PlayerProgressFinishCap;
import org.galaxy.beyond.api.system.rogue.cap.ProgressStartCap;
import org.galaxy.beyond.api.system.rogue.cap.RogueInitCap;
import org.galaxy.beyond.api.system.rogue.cap.RogueProgressFinishCap;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BeyondRogueCapInit {

    public static final Supplier<RogueCap> PROGRESS_START;
    public static final Supplier<RogueCap> PLAYER_IN_GAME;
    public static final Supplier<RogueCap> NODE_CAP;
    public static final Supplier<RogueCap> NODE_ZONE_ENTER;
    public static final Supplier<RogueCap> ROGUE_INIT;
    public static final Supplier<RogueCap> ROGUE_PROGRESS_FINISH;
    public static final Supplier<RogueCap> PLAYER_PROGRESS_FINISH;

    static {
        PROGRESS_START         = registerCap(ProgressStartCap::new);
        PLAYER_IN_GAME         = registerCap(PlayerInGameCap::new);
        NODE_CAP               = registerCap(NodeCap::new);
        NODE_ZONE_ENTER        = registerCap(NodeZoneEnterCap::new);
        ROGUE_INIT             = registerCap(RogueInitCap::new);
        ROGUE_PROGRESS_FINISH  = registerCap(RogueProgressFinishCap::new);
        PLAYER_PROGRESS_FINISH = registerCap(PlayerProgressFinishCap::new);
    }

    public static void register(IEventBus eventBus) {
    }

    public static Optional<Holder.Reference<RogueCap>> getById(Identifier id) {
        return BeyondRegistries.ROGUE_CAP.get(id);
    }

    public static List<RogueCap> getAll() {
        return BeyondRegistries.ROGUE_CAP.stream().collect(Collectors.toList());
    }

    public static Supplier<RogueCap> registerCap(Supplier<? extends RogueCap> sup) {
        return BeyondRegistries.ROGUE_CAP_REGISTER.register(sup.get().getId().getPath(), sup);
    }

    public static Supplier<RogueCap> registerCap(Identifier id, Supplier<? extends RogueCap> sup) {
        return BeyondRegistries.ROGUE_CAP_REGISTER.register(id.getPath(), sup);
    }
}
