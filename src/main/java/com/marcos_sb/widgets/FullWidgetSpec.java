package com.marcos_sb.widgets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FullWidgetSpec {
    @JsonProperty(required = true)
    private final long x;
    @JsonProperty(required = true)
    private final long y;
    @JsonProperty(required = true)
    private final double width;
    @JsonProperty(required = true)
    private final double height;

    private final Integer zIndex;

    @JsonCreator
    public FullWidgetSpec(long x, long y, double width, double height, Integer zIndex) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zIndex = zIndex;
    }

    @Override
    public String toString() {
        return String.format("[x:%d, y:%d, w:%.2f, h:%.2f, z:%d]",
            x, y, width, height, zIndex);
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Integer getzIndex() {
        return zIndex;
    }

    @JsonIgnore
    public boolean hasZIndex() {
        return zIndex != null;
    }
}
