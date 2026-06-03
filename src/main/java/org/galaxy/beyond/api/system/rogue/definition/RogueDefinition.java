package org.galaxy.beyond.api.system.rogue.definition;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.ReadOnlyManaged;
import lombok.Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.galaxy.beyond.api.util.CompatUtil;

import java.util.HashMap;
import java.util.Map;

@Data
public class RogueDefinition implements IPersistedSerializable {

    @Persisted
    @ReadOnlyManaged(serializeMethod = "rogueProgressSerialize", deserializeMethod = "rogueProgressDeserialize")
    private final Map<Identifier, ProgressDefinition> rogueProgress = new HashMap<>();

    // ============================================================
    // 校验 + 获取
    // ============================================================

    public boolean hasProgress(Identifier id) {
        return rogueProgress.containsKey(id);
    }

    public ProgressDefinition getProgress(Identifier id) {
        return rogueProgress.get(id);
    }

    /** 校验 currentProgress 是否在 definition 中存在，不存在返回错误消息 */
    public Component validateProgress(Identifier id) {
        if (id == null) {
            return Component.translatable("beyond.definition.progress_not_set");
        }
        if (!hasProgress(id)) {
            return Component.translatable("beyond.definition.progress_not_found", id.toString());
        }
        return null;
    }

    public CompoundTag rogueProgressSerialize(Map<Identifier, ProgressDefinition> m) {
        var keys = new ListTag();
        m.keySet().forEach(k -> keys.add(StringTag.valueOf(k.toString())));
        var c = new CompoundTag();
        c.put("keys", keys);
        return c;
    }

    public Map<Identifier, ProgressDefinition> rogueProgressDeserialize(CompoundTag c) {
        var m = new HashMap<Identifier, ProgressDefinition>();
        var keys = CompatUtil.getStringListOrEmpty(c, "keys");
        for (Tag e : keys) {
            m.put(Identifier.parse(CompatUtil.asString(e)), new ProgressDefinition());
        }
        return m;
    }
}
