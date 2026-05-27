package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.api.event.custom.LivingChangeZoneEvent;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.Phase;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.List;

/**
 * IRogueCap 事件分发器 —— 从 {@link RogueData#rogueCapData} 读取全局 cap 列表并分发事件。
 */
public class RogueCapManager {

    private final IRogueContext ctx;

    public RogueCapManager(IRogueContext ctx) {
        this.ctx = ctx;
    }

    // ============================================================
    // Zone 事件
    // ============================================================

    public void tickZoneEvents(ServerLevel level) {
        var caps = getCaps(level);
        LevelZoneData lzd = getLZD(level);

        dispatchLevelTick(caps, level);

        for (ServerPlayer player : level.players()) {
            var mobData = BeyondAPI.getBeyondMobData(player);
            ZoneType oldZone = mobData.getZoneType();
            ZoneType newZone = lzd.getZoneType(org.galaxy.beyond.api.util.CompatUtil.chunkPos(player.getOnPos()));
            if (newZone == null) newZone = ZoneType.Empty;

            dispatchLivingTick(caps, player);

            if (oldZone != newZone) {
                mobData.setZoneType(newZone);
                NeoForge.EVENT_BUS.post(new LivingChangeZoneEvent(player, oldZone, newZone));
                dispatchChangeZone(caps, player, oldZone, newZone);
            }
        }
    }

    public void tickMob(Mob mob) {
        ServerLevel level = (ServerLevel) mob.level();
        LevelZoneData lzd = getLZD(level);
        ZoneType newZone = lzd.getZoneType(org.galaxy.beyond.api.util.CompatUtil.chunkPos(mob.blockPosition()));
        if (newZone == null) return;

        var caps = getCaps(level);
        var mobData = BeyondAPI.getBeyondMobData(mob);
        if (mobData.getZoneType() != newZone) {
            dispatchChangeZone(caps, mob, mobData.getZoneType(), newZone);
            mobData.setZoneType(newZone);
        }
        dispatchLivingTick(caps, mob);
    }

    // ============================================================
    // Phase 事件分发
    // ============================================================

    public void dispatchPhaseEnter(ServerLevel level, Phase from, Phase to) {
        for (RogueCapData cd : getCaps(level))
            cd.getCap().phaseEnter(level, from, to, ctx);
    }

    public void dispatchPhaseExit(ServerLevel level, Phase from, Phase to) {
        for (RogueCapData cd : getCaps(level))
            cd.getCap().phaseExit(level, from, to, ctx);
    }

    public void dispatchPhaseTick(ServerLevel level, Phase phase) {
        for (RogueCapData cd : getCaps(level))
            cd.getCap().phaseTick(level, phase, ctx);
    }

    // ============================================================
    // 内部
    // ============================================================

    private List<RogueCapData> getCaps(ServerLevel level) {
        return BeyondAPI.getRogueData(level).getRogueCapData();
    }

    private static LevelZoneData getLZD(ServerLevel level) {
        return BeyondAPI.getLevelZoneData(level);
    }

    private void dispatchLevelTick(List<RogueCapData> caps, ServerLevel level) {
        for (RogueCapData cd : caps)
            cd.getCap().levelTick(level, ctx);
    }

    private void dispatchLivingTick(List<RogueCapData> caps, LivingEntity entity) {
        for (RogueCapData cd : caps)
            cd.getCap().livingTick(entity, ctx);
    }

    private void dispatchChangeZone(List<RogueCapData> caps, LivingEntity entity, ZoneType from, ZoneType to) {
        for (RogueCapData cd : caps)
            cd.getCap().changeZone(entity, from, to, ctx);
    }
}
