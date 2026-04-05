package com.pz.beyond.data;

/**
 * 客户端的缓存数据
 */
public class SafeZoneClientCache {
    private static int centerX = 0;
    private static int centerZ = 0;
    private static int radius  = 0;


    public static void update(int cx,int cz, int r) {
        centerX = cx;
        centerZ = cz;
        radius = r;
    }

    public static int getCenterX() {
        return centerX;
    }

    public static int getCenterZ() {
        return centerZ;
    }

    public static int getRadius() {
        return radius;
    }
}
