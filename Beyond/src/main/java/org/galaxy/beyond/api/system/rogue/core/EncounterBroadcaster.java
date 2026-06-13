package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.EncounterType;

import java.util.List;

public final class EncounterBroadcaster {

    public void encounterStart(ServerLevel level, EncounterType type) {
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.encounter_start",
                        Component.translatable(type.getTranslationKey())), false);
    }

    public void currentEvent(ServerLevel level, List<ResourceLocation> eventIds, int index, int total) {
        if (index >= eventIds.size()) {
            return;
        }
        String eventPath = eventIds.get(index).getPath();
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.event_trigger",
                        Component.translatable("beyond.event." + eventPath),
                        index + 1, total), false);
    }

    public void nextEvent(ServerLevel level) {
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.next_event"), false);
    }

    public void nodeUnlocked(ServerLevel level, int current, int totalScenes) {
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.unlocked"), false);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.progress_advance", current, totalScenes), false);
    }
}
