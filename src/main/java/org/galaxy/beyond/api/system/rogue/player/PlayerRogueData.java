package org.galaxy.beyond.api.system.rogue.player;

import lombok.Data;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;

@Data
public class PlayerRogueData {

    private PlayerRogueState state = PlayerRogueState.LOBBY;
    private int lifeCount;          // 剩余复活次数
    private int maxLifeCount;       // 最大复活次数（属性驱动）
    private int deathCount;         // 累计死亡次数
    private long readyTimestamp;    // ready的时间戳，用于超时检测
}
