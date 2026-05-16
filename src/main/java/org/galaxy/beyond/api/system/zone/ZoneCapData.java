package org.galaxy.beyond.api.system.zone;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.Identifier;
import org.galaxy.beyond.api.init.BeyondZoneNodeCapInit;

import java.util.Objects;

@Getter
@Setter
public class ZoneCapData implements IPersistedSerializable {

    @DescSynced
    @Persisted
    private Identifier capTypeId;
    @DescSynced
    @Persisted
    private int capLevel;

    private ZoneCapType type;

    public ZoneCapData() {}

    public ZoneCapData(ZoneCapType type) {
        this.type = type;
        this.capTypeId = type.getId();
        this.capLevel = 1;
    }

    public ZoneCapType getType() {
        if (type == null && capTypeId != null) {
            type = BeyondZoneNodeCapInit.getById(capTypeId)
                    .map(ref -> ref.value())
                    .orElse(null);
        }
        return type;
    }

    //todo 事件支持
    public void setLevel(int level) {
        this.capLevel = level;
    }

    public void addLevel(int level) {
        setLevel(this.capLevel + level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ZoneCapData that)) return false;
        return Objects.equals(capTypeId, that.capTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(capTypeId);
    }
}
