package org.galaxy.beyond.api.system.rogue.definition;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.ReadOnlyManaged;
import lombok.Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.rogue.SceneType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RogueDefinition implements IPersistedSerializable {

    @Persisted
    @ReadOnlyManaged(serializeMethod = "rogueProgressSerialize", deserializeMethod = "rogueProgressDeserialize")
    private final Map<Identifier, ProgressDefinition> rogueProgress = new HashMap<>();

    public CompoundTag rogueProgressSerialize(Map<Identifier, ProgressDefinition> m) {
        var keys = new ListTag();
        m.keySet().forEach(k -> keys.add(StringTag.valueOf(k.toString())));
        var c = new CompoundTag();
        c.put("keys", keys);
        return c;
    }

    public Map<Identifier, ProgressDefinition> rogueProgressDeserialize(CompoundTag c) {
        var m = new HashMap<Identifier, ProgressDefinition>();
        var keys = c.getListOrEmpty("keys");
        for (Tag e : keys) {
            m.put(Identifier.parse(e.asString().orElse("")), new ProgressDefinition());
        }
        return m;
    }
}
