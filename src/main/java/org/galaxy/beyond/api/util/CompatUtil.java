package org.galaxy.beyond.api.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public final class CompatUtil {
    private CompatUtil() {
    }

    public static ChunkPos chunkPos(BlockPos pos) {
        return new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static ListTag getCompoundListOrEmpty(CompoundTag tag, String key) {
        return tag.getList(key).orElseGet(ListTag::new);
    }

    public static ListTag getStringListOrEmpty(CompoundTag tag, String key) {
        return tag.getList(key).orElseGet(ListTag::new);
    }

    public static String asString(Tag tag) {
        return tag.asString().orElse("");
    }

    public static boolean hasPermission(CommandSourceStack source, int level) {
        return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(level)));
    }

    public static void sendActionBar(ServerPlayer player, Component message) {
        player.sendSystemMessage(message, true);
    }
}
