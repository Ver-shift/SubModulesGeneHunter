package org.galaxy.beyond.api.system.zone;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class ZoneCapData {
    private final ZoneCapType type;
    private int capLevel;

    public ZoneCapData(ZoneCapType type) {
        this.type = type;
        this.capLevel = 1;
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
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
