package com.pz.beyond.api.system.node;

/**
 * 节点颜色
 */
public enum NodeColor {
    /**
     * 绿色：安全，基本上没有风险
     */
    GREEN(0x00FF00),
    /**
     * 蓝色：已解锁过的节点（完成后的渲染状态）
     */
    BLUE(0x0088FF),
    /**
     * 橙色：机遇，未解锁的挑战等待解锁
     */
    ORANGE(0xFF8800),
    /**
     * 红色：危险，有更多的机遇和更多的挑战
     */
    RED(0xFF0000);

    private final int color;

    NodeColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
