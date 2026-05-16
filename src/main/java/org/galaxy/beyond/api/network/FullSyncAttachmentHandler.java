package org.galaxy.beyond.api.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * 通用 AttachmentSyncHandler：每次同步都发送完整数据。
 * 相比直接传 StreamCodec 的好处是显式控制 sendToPlayer 和 previousValue 合并策略。
 */
public class FullSyncAttachmentHandler<T> implements AttachmentSyncHandler<T> {

    private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
    private final Supplier<T> defaultSupplier;
    private final BiPredicate<IAttachmentHolder, ServerPlayer> sendToPlayer;

    public FullSyncAttachmentHandler(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec,
                                     Supplier<T> defaultSupplier,
                                     BiPredicate<IAttachmentHolder, ServerPlayer> sendToPlayer) {
        this.streamCodec = streamCodec;
        this.defaultSupplier = defaultSupplier;
        this.sendToPlayer = sendToPlayer;
    }

    public FullSyncAttachmentHandler(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec,
                                     Supplier<T> defaultSupplier) {
        this(streamCodec, defaultSupplier, (holder, player) -> true);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf, T attachment, boolean initialSync) {
        streamCodec.encode(buf, attachment);
    }

    @Override
    @Nullable
    public T read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable T previousValue) {
        T decoded = streamCodec.decode(buf);
        if (previousValue != null && decoded != null) {
            return merge(previousValue, decoded);
        }
        return decoded != null ? decoded : defaultSupplier.get();
    }

    @Override
    public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        return sendToPlayer.test(holder, to);
    }


    /**
     * 子类可重写此方法实现增量合并，默认直接返回 decoded（完整替换）。
     */
    protected T merge(T previousValue, T decoded) {
        return decoded;
    }
}
