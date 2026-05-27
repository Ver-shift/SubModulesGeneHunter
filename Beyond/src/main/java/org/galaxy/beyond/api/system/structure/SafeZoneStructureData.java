package org.galaxy.beyond.api.system.structure;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;

@Data
public class SafeZoneStructureData implements IPersistedSerializable {

    @Persisted
    private int initialized;
    @Persisted
    private int spawnX;
    @Persisted
    private int spawnY;
    @Persisted
    private int spawnZ;
    @Persisted
    private int centerX;
    @Persisted
    private int centerY;
    @Persisted
    private int centerZ;

    public static final MapCodec<SafeZoneStructureData> CODEC = PersistedParser.createMapCodec(SafeZoneStructureData::new);
    public static final StreamCodec<ByteBuf, SafeZoneStructureData> STREAM_CODEC = PersistedParser.createStreamCodec(SafeZoneStructureData::new);

    // ---- BlockPos 辅助 ----

    public BlockPos getSpawnPos() {
        return new BlockPos(spawnX, spawnY, spawnZ);
    }

    public void setSpawnPos(BlockPos pos) {
        this.spawnX = pos.getX();
        this.spawnY = pos.getY();
        this.spawnZ = pos.getZ();
    }

    public BlockPos getCenterPos() {
        return new BlockPos(centerX, centerY, centerZ);
    }

    public void setCenterPos(BlockPos pos) {
        this.centerX = pos.getX();
        this.centerY = pos.getY();
        this.centerZ = pos.getZ();
    }
}
