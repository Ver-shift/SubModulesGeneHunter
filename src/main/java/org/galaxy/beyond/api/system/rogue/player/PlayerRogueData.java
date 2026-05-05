package org.galaxy.beyond.api.system.rogue.player;

import lombok.Data;

@Data
public class PlayerRogueData {

    private PlayerRogueState state;
    private int lifeCount; //生命数量
    private int deathCount;//死亡次数，达到死亡次数就会死。

}
