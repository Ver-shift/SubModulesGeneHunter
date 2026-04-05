package org.biotech.api.system.gene;

import net.minecraft.server.level.ServerPlayer;


/**
 * Gene Context for gene operations
 * @param player 玩家

 */
public record GeneContext(ServerPlayer player) {
}
