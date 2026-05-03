package org.galaxy.beyond.api.system.zone;

import lombok.Data;

@Data
public class ZoneCapData {
    private ZoneCapType type;
    private int capLevel;

    public ZoneCapData(ZoneCapType type) {
        this.type = type;
        this.capLevel = 1;
    }

    //todo 事件支持
    public void setLevel(int level){
        this.capLevel = level;
    }

    public void addLevel(int level){
        setLevel(this.capLevel + level);
    }
}
