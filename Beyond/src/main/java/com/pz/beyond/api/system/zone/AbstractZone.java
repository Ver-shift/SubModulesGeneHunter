package com.pz.beyond.api.system.zone;

import com.pz.beyond.api.system.zone.core.IZone;
import com.pz.beyond.api.system.zone.core.IZoneChunkQuery;
import com.pz.beyond.api.system.rule.IZoneRule;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;


public abstract class AbstractZone<T extends IZoneRule> implements IZone<T>, IZoneChunkQuery {

    /**
     * 标识符
     */
    protected final ResourceLocation identifier;

    /**
     * 监听器数组 - Copy-On-Write，保证读性能
     * 内部使用 IZoneEventListener[] 存储（因为泛型数组无法直接实例化）
     */
    private volatile IZoneRule[] listeners = new IZoneRule[0];

    /**
     * 区块集合，存储该Zone包含的所有完整区块
     * 每个long是ChunkPos.asLong(chunkX, chunkZ)生成的chunkKey
     */
    private final LongOpenHashSet chunkKeys = new LongOpenHashSet();


    public AbstractZone(ResourceLocation identifier) {
        this.identifier = identifier;
    }




    @Override
    public void addListener(T listener) {
        synchronized (this) {
            IZoneRule[] newListeners = new IZoneRule[listeners.length + 1];
            System.arraycopy(listeners, 0, newListeners, 0, listeners.length);
            newListeners[listeners.length] = listener;
            listeners = newListeners;
        }
    }

    @Override
    public void removeListener(T listener) {
        synchronized (this) {
            int index = -1;
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == listener) {
                    index = i;
                    break;
                }
            }
            if (index == -1) return;

            IZoneRule[] newListeners = new IZoneRule[listeners.length - 1];
            System.arraycopy(listeners, 0, newListeners, 0, index);
            System.arraycopy(listeners, index + 1, newListeners, index, listeners.length - index - 1);
            listeners = newListeners;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] getListeners() {
        return (T[]) listeners;
    }

    /**
     * 设置监听器数组（用于反序列化）
     */
    protected void setListeners(T[] listeners) {
        this.listeners = listeners;
    }



    @Override
    public void addChunk(int chunkX, int chunkZ) {
        chunkKeys.add(ChunkPos.asLong(chunkX, chunkZ));
    }

    @Override
    public void addChunk(ChunkPos pos) {
        chunkKeys.add(pos.toLong());
    }

    @Override
    public void removeChunk(int chunkX, int chunkZ) {
        chunkKeys.remove(ChunkPos.asLong(chunkX, chunkZ));
    }

    @Override
    public void removeChunk(ChunkPos pos) {
        chunkKeys.remove(pos.toLong());
    }

    @Override
    public boolean contains(BlockPos pos) {
        return chunkKeys.contains(ChunkPos.asLong(pos));
    }

    @Override
    public boolean containsChunk(ChunkPos pos) {
        return chunkKeys.contains(pos.toLong());
    }

    @Override
    public boolean containsChunk(int chunkX, int chunkZ) {
        return chunkKeys.contains(ChunkPos.asLong(chunkX, chunkZ));
    }

    @Override
    public int getChunkCount() {
        return chunkKeys.size();
    }

    @Override
    public boolean isEmpty() {
        return chunkKeys.isEmpty();
    }

    @Override
    public LongIterator getChunkKeyIterator() {
        return chunkKeys.iterator();
    }

    @Override
    public LongOpenHashSet getChunkKeys() {
        return chunkKeys.clone();
    }

    @Override
    public ResourceLocation getIdentifier() {
        return identifier;
    }
}
