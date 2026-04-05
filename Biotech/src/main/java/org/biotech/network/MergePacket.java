package org.biotech.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.biotech.Biotech;
import org.biotech.api.BiotechAPI;
import org.biotech.util.TodoDebugLog;

/**
 * Client -> Server merge request packet.
 */
public record MergePacket() implements CustomPacketPayload {

	public static final Type<MergePacket> TYPE = new Type<>(Biotech.asResource("merge"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MergePacket> STREAM_CODEC = StreamCodec.unit(new MergePacket());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(MergePacket payload, IPayloadContext context) {
		context.enqueueWork(() -> {
			var player = context.player();
			TodoDebugLog.info("merge_packet", () -> "received from=" + player.getName().getString());
			var manager = BiotechAPI.getMergeManager(player);
			if (manager == null) {
				TodoDebugLog.warn("merge_packet", () -> "merge manager is null");
				return;
			}
			manager.merge();
			manager.updateSlotData();
			var mergeData = BiotechAPI.getGeneData(player).getMergeData();
			TodoDebugLog.info("merge_packet", () -> "merge handled: traits="
					+ mergeData.getCachedTraitCount()
					+ ", perGene=" + mergeData.getCachedTraitCountPerGene()
					+ ", output=" + mergeData.getCachedOutputXeneCount()
					+ ", geneItems=" + mergeData.getCachedGeneItemCount());
		});
	}
}
