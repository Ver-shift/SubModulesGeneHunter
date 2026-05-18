package org.galaxy.beyond.api.init;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.zone.ZoneCapType;
import org.galaxy.beyond.api.system.rogue.cap.NodeCap;
import org.galaxy.beyond.api.system.rogue.cap.PlayerInGameCap;
import org.galaxy.beyond.api.system.rogue.cap.ProgressStartCap;
import org.galaxy.beyond.zone_cap.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BeyondZoneNodeCapInit {

    public static void register(IEventBus eventBus) {
        // DeferredRegister 已在 BeyondRegistries 注册到 eventBus
    }

    public static Optional<Holder.Reference<ZoneCapType>> getById(Identifier id) {
        return BeyondRegistries.ZONE_CAP_TYPE.get(id);
    }

    public static List<ZoneCapType> getAll() {
        return BeyondRegistries.ZONE_CAP_TYPE.stream().collect(Collectors.toList());
    }

    public static Supplier<ZoneCapType> registerCap(Supplier<? extends ZoneCapType> sup) {
        return BeyondRegistries.ZONE_CAP_TYPE_REGISTER.register(sup.get().getId().getPath(), sup);
    }

    public static final Supplier<ZoneCapType> ALL_NODE_ZONE    = registerCap(AllNodeZoneCap::new);
    public static final Supplier<ZoneCapType> ALL_SAFE_ZONE    = registerCap(AllSafeZoneCap::new);
    public static final Supplier<ZoneCapType> PROGRESS_START   = registerCap(ProgressStartCap::new);
    public static final Supplier<ZoneCapType> PLAYER_IN_GAME   = registerCap(PlayerInGameCap::new);
    public static final Supplier<ZoneCapType> NODE_CAP         = registerCap(NodeCap::new);
}
