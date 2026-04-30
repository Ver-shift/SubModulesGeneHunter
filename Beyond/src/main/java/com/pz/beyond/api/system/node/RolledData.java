package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.init.BeyondEncounters;
import com.pz.beyond.api.system.definition.EncounterDefinition;
import com.pz.beyond.api.system.definition.EventTask;
import com.pz.beyond.api.system.definition.ProgressDefinition;
import com.pz.beyond.api.system.progress.SceneType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

/**
 * 节点的运行时数据，玩家交互节点后产生。
 * 方案 C：只持有 structureKey，NodeData 统一从 {@link com.pz.beyond.api.system.progress.ProgressData#getNodes()} 反查，
 * 避免 Codec 重复序列化导致的引用失效（状态改动无法回写）。
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RolledData {

    public static final String STRUCTURE_KEY = "structure_key";
    public static final String ENCOUNTER_TYPE = "encounter_type";
    public static final String EVENTS = "events";
    public static final String CURRENT_EVENT_INDEX = "current_event_index";


    private StructureKey structureKey = StructureKey.EMPTY;
    private EncounterType encounterType = BeyondEncounters.EMPTY;

    private EventTask events = EventTask.EMPTY;

    private int currentEventIndex = 0;


    public static RolledData create(RolledData data, NodeData nodeData, SceneType sceneType,
                                    ProgressDefinition progressDefinition,
                                    SingleThreadedRandomSource random){
        if (data == null) {
            data = new RolledData();
        }
        // 1. 绑定节点身份（只存 StructureKey，避免 Codec 重复序列化）
        data.setStructureKey(nodeData == null ? StructureKey.EMPTY : nodeData.getStructureKey());

        // 2. 根据 (节点颜色, 场景) 挑选 EncounterType
        EncounterType encounterType = BeyondEncounters.getTypeByColorAndScene(
                nodeData == null ? null : nodeData.getNodeColor(), sceneType, random);
        data.setEncounterType(encounterType);

        // 3. 从 ProgressDefinition 的 encounters 映射中找到对应 EncounterDefinition，再加权抽取一个 EventTask
        EventTask eventTask = EventTask.EMPTY;
        if (progressDefinition != null && encounterType != null && encounterType.getIdentifier() != null) {
            for (ProgressDefinition.EncounterMapping mapping : progressDefinition.getEncounters()) {
                if (mapping == null) continue;
                EncounterType mapType = mapping.getEncounterType();
                if (mapType == null || mapType.getIdentifier() == null) continue;
                if (!mapType.getIdentifier().equals(encounterType.getIdentifier())) continue;

                EncounterDefinition def = mapping.getEncounterDefinition();
                if (def != null) {
                    // 走 ITranslate 通用入口：从 definition roll 出运行时 EventTask
                    eventTask = def.translate(random);
                }
                break;
            }
        }
        data.setEvents(eventTask);
        // 4. 重置进度指针
        data.setCurrentEventIndex(0);

        return data;
    }


    public static final Codec<RolledData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    StructureKey.CODEC.optionalFieldOf(STRUCTURE_KEY, StructureKey.EMPTY).forGetter(RolledData::getStructureKey),
                    EncounterType.CODEC.optionalFieldOf(ENCOUNTER_TYPE, BeyondEncounters.EMPTY).forGetter(RolledData::getEncounterType),
                    EventTask.CODEC.optionalFieldOf(EVENTS, EventTask.EMPTY).forGetter(RolledData::getEvents),
                    Codec.INT.optionalFieldOf(CURRENT_EVENT_INDEX, 0).forGetter(RolledData::getCurrentEventIndex)
            ).apply(instance, RolledData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RolledData> STREAM_CODEC = StreamCodec.composite(
            StructureKey.STREAM_CODEC, RolledData::getStructureKey,
            EncounterType.STREAM_CODEC, RolledData::getEncounterType,
            EventTask.STREAM_CODEC, RolledData::getEvents,
            ByteBufCodecs.VAR_INT, RolledData::getCurrentEventIndex,
            RolledData::new
    );
}
