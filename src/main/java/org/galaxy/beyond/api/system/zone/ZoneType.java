package org.galaxy.beyond.api.system.zone;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;

/**
 * 区域类型枚举，每个类型携带一个 bitmask 值，用于快速位运算。
 * <pre>{@code
 *   Safe_Zone  = 0b0001 (1)
 *   Node_Zone  = 0b0010 (2)
 *   Active_Zone= 0b0100 (4)
 *   Empty      = 0b0000 (0)
 * }</pre>
 * 可通过 {@code ZoneType.of(mask)} 组合多个类型进行批量匹配。
 */
public enum ZoneType implements IPersistedSerializable {
    Safe_Zone("safe_zone", (byte) 1),
    Node_Zone("node_zone", (byte) 2),
    Active_Zone("active_zone", (byte) 4),
    Empty("empty", (byte) 0);

    ZoneType(final String name, final byte mask) {
        this.name = name;
        this.mask = mask;
    }

    @Persisted
    private final String name;
    private final byte mask;

    public String getName() {
        return name;
    }

    /** 返回位掩码值，用于快速位运算判断类型组合。 */
    public byte mask() {
        return mask;
    }

    /** 判断当前类型是否被 targetMask 包含（按位与）。 */
    public boolean matches(byte targetMask) {
        return (this.mask & targetMask) != 0;
    }

    /**
     * 将多个 ZoneType 合并为一个 bitmask。
     * <pre>{@code ZoneType.of(Safe_Zone, Node_Zone)}</pre>
     */
    public static byte of(ZoneType first, ZoneType... rest) {
        byte m = first.mask;
        for (ZoneType t : rest) m |= t.mask;
        return m;
    }
}
