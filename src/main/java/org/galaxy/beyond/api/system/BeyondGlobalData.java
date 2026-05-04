package org.galaxy.beyond.api.system;

import lombok.Data;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.rogue.definition.RogueDefinition;

/**
 * 所有数据都存放在overWorld。在其他维度也能通过服务器获取数据。
 */
@Data
public class BeyondGlobalData {

    public static final ResourceKey<Level> OVERWORLD = Level.OVERWORLD;

    private RogueDefinition rogueDefinition = new RogueDefinition();


}
