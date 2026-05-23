package org.galaxy.beyond.api.system.zone;

/**
 * 区域类型 —— 三种独立存储，同一区块可叠加多种类型。
 */
public enum ZoneType {
    Safe_Zone,
    Node_Zone,
    Active_Zone,
    Empty;

    /** 兼容旧 API 的位掩码匹配 */
    public boolean matches(byte mask) {
        if (this == Safe_Zone)  return (mask & 1) != 0;
        if (this == Node_Zone)  return (mask & 2) != 0;
        if (this == Active_Zone)return (mask & 4) != 0;
        return false;
    }

    public byte mask() {
        return (byte) switch (this) {
            case Safe_Zone   -> 1;
            case Node_Zone   -> 2;
            case Active_Zone -> 4;
            default          -> 0;
        };
    }

    public static byte of(ZoneType first, ZoneType... rest) {
        byte m = first.mask();
        for (ZoneType t : rest) m |= t.mask();
        return m;
    }
}
