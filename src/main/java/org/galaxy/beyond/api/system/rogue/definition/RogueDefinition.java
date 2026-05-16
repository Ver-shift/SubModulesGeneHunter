package org.galaxy.beyond.api.system.rogue.definition;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Data;
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

    @Persisted(subPersisted = true)
    private final Map<Identifier, ProgressDefinition> rogueProgress = new HashMap<>();

}
