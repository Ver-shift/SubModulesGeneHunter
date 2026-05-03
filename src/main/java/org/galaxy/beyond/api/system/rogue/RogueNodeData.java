package org.galaxy.beyond.api.system.rogue;

import lombok.Data;
import org.galaxy.beyond.api.system.node.NodeData;

@Data
public class RogueNodeData {

    private NodeData nodeData;
    private EncounterData encounterData;

}
