package org.galaxy.beyond.api.system.node;

public enum NodeColor {
    BLUE(0x0000FF),
    GREEN(0x00FF00),
    ORANGE(0xFFA500),
    RED(0xFF0000);

    private final int colorValue;

    NodeColor(int colorValue) {
        this.colorValue = colorValue;
    }

    public int getColorValue() {
        return colorValue;
    }
}
