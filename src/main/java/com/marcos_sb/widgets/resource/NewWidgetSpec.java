package com.marcos_sb.widgets.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

public class NewWidgetSpec {

    @JsonProperty(required = true) @NotNull
    private final long x;

    @JsonProperty(required = true) @NotNull
    private final long y;

    @JsonProperty(required = true) @DecimalMin("0")
    private final double width;

    @JsonProperty(required = true) @DecimalMin("0")
    private final double height;

    @JsonProperty("z-index")
    private final Integer zIndex;

    @JsonCreator
    public NewWidgetSpec(long x, long y, double width, double height,
                         @JsonProperty("z-index") Integer zIndex) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zIndex = zIndex;
    }

    public NewWidgetSpec(long x, long y, double width, double height) {
        this(x, y, width, height, null);
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

    @JsonIgnore
    public Integer getzIndex() {
        return zIndex;
    }

    @JsonIgnore
    public boolean hasZIndex() {
        return zIndex != null;
    }
}
