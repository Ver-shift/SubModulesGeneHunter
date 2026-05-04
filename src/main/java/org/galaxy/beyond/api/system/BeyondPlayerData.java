package org.galaxy.beyond.api.system;

import lombok.Data;
import org.galaxy.beyond.api.system.rogue.player.PlayerRogueData;
import org.galaxy.beyond.api.system.statistics.PlayerStatisticsData;
import org.galaxy.beyond.api.system.zone.PlayerZoneData;

@Data
public class BeyondPlayerData {

    private final PlayerRogueData playerRogueData = new PlayerRogueData();
    private final PlayerZoneData playerZoneData = new PlayerZoneData();
    private final PlayerStatisticsData playerStatisticsData = new PlayerStatisticsData();
}
