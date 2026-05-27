package org.galaxy.beyond.api.system.large;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BeyondLargeLevelDataSyncHandler implements AttachmentSyncHandler<BeyondLargeLevelData> {

    @Override
    public void write(RegistryFriendlyByteBuf buf, BeyondLargeLevelData attachment, boolean initialSync) {
        if (initialSync) {
            buf.writeBoolean(true);
            attachment.writeFull(buf);
            return;
        }

        List<LargeDataDelta> deltas = attachment.getSyncSnapshot();
        if (deltas.isEmpty()) {
            buf.writeBoolean(false);
            buf.writeVarInt(0);
            return;
        }

        buf.writeBoolean(false);
        buf.writeVarInt(deltas.size());
        for (LargeDataDelta delta : deltas) {
            delta.write(buf);
        }
    }

    @Override
    public @Nullable BeyondLargeLevelData read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf,
                                               @Nullable BeyondLargeLevelData previousValue) {
        boolean full = buf.readBoolean();
        if (full || previousValue == null) {
            return BeyondLargeLevelData.readFull(buf);
        }

        int deltaCount = buf.readVarInt();
        for (int i = 0; i < deltaCount; i++) {
            previousValue.applyDelta(LargeDataDelta.read(buf));
        }
        return previousValue;
    }
}
