package com.pz.beyond.api.system.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.zone.core.IRuleContainer;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * 区域的数据，每个区域都有一个数据
 */
@Data
public class ZoneData implements IRuleContainer {

    public static final String ZONE_TYPE = "zone_type";
    public static final String LISTENERS = "listeners";

    private ZoneType zone = BeyondZoneInit.EMPTY;
    private List<RuleData> listeners = new CopyOnWriteArrayList<>();


    @Override
    public List<RuleData> getListeners() {
        return listeners;
    }

    @Override
    public void addListener(RuleData listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(RuleData listener) {
        listeners.remove(listener);
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }

    // 工厂方法专用构造
    public ZoneData(ZoneType zone, List<RuleData> listeners) {
        this.zone = zone == null ? BeyondZoneInit.EMPTY : zone;
        this.listeners = listeners == null ? new CopyOnWriteArrayList<>() : new CopyOnWriteArrayList<>(listeners);
    }

    public static final Codec<ZoneData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ZoneType.CODEC.fieldOf(ZONE_TYPE).forGetter(ZoneData::getZone),
            Codec.list(RuleData.CODEC).fieldOf(LISTENERS).forGetter(ZoneData::getListeners)
    ).apply(builder, ZoneData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ZoneData> STREAM_CODEC = StreamCodec.composite(
            ZoneType.STREAM_CODEC,
            ZoneData::getZone,
            ByteBufCodecs.collection(ArrayList::new, RuleData.STREAM_CODEC),
            ZoneData::getListeners,
            ZoneData::new
    );

}
