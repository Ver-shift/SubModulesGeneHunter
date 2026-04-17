package com.pz.beyond.api.system.zone.zones;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.config.ServerConfig;
import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.init.BeyondZoneRuleInit;
import com.pz.beyond.api.system.progress.ProgressState;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.zone.ZoneType;
import com.pz.beyond.api.system.zone.LevelZoneData;
import com.pz.beyond.api.util.BorderRenderUtil;
import com.pz.beyond.api.util.TeleportUtil;
import net.minecraft.client.Camera;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 安全区域
 * <p>
 * 负责安全区的初始化和玩家传送
 */
public class SafeZone extends ZoneType {

    public static final ResourceLocation SAFE_ZONE = Beyond.asResource("safe_zone");
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeZone.class);

    public SafeZone() {
        super(SAFE_ZONE);
    }

    /**
     * 初始化安全区规则
     * 添加默认的规则监听器
     * 
     * @param listeners 规则监听器列表
     * @param level 服务端维度
     */
    @Override
    public void initialize(List<RuleData> listeners, ServerLevel level) {
        LOGGER.debug("SafeZone 初始化规则");
        // 添加安全区规则：全部安全
        addRule(listeners, new RuleData(BeyondZoneRuleInit.ALL_SAFE_RULE.get()));
    }



}
