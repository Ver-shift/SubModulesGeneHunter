package org.galaxy.beyond.api.system.rogue;

import lombok.Data;

import java.util.LinkedList;

@Data
public class EncounterData {
    private EncounterType type;
    private EventTask events;
}
