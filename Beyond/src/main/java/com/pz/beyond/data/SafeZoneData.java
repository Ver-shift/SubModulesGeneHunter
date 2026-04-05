package com.pz.beyond.data;

import com.pz.beyond.util.SafeZonePayloadUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 安全区的一些设定
 * <p>
 * 是一个正方形范围
 */
public class SafeZoneData extends SavedData {

    private int centerX = 0;
    private int centerZ = 0;
    private int radius = 0;
    // 标记是否有初始化
    private boolean isFirst = false;
    public SafeZoneData() {
    }

    public static SafeZoneData create() {
        return new SafeZoneData();
    }

    public static SafeZoneData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        SafeZoneData data = SafeZoneData.create();
        data.centerX = tag.getInt("centerX");
        data.centerZ = tag.getInt("centerZ");
        data.radius = tag.getInt("radius");
        data.isFirst = tag.getBoolean("isFirst");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("centerX", this.centerX);
        tag.putInt("centerZ", this.centerZ);
        tag.putInt("radius", this.radius);
        tag.putBoolean("isFirst",this.isFirst);
        return tag;
    }


    public boolean isWithinSafeZone(BlockPos pos) {
        return isWithinSafeZone(pos.getX(),pos.getZ());
    }

    public boolean isWithinSafeZone(double x, double z) {
        double dx = Math.abs(x - centerX);
        double dz = Math.abs(z - centerZ);
        return dx <= radius && dz <= radius;
    }


    public void updateCenter(BlockPos pos, ServerLevel serverLevel) {
        updateCenter(pos.getX(),pos.getZ());
        SafeZonePayloadUtil.SafeZoneToAll(serverLevel,this);
    }

    public void updateCenter(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
        this.setDirty();
    }

    public void addRadius(int r, ServerLevel serverLevel) {
         addRadius(r);
        SafeZonePayloadUtil.SafeZoneToAll(serverLevel,this);
    }

    private void addRadius(int radius) {
        this.radius = Math.max(0, this.radius += radius);
        this.setDirty();
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }
}
