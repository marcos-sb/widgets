package com.marcos_sb.widgets.model.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Widget {

    private final UUID uuid;
    private final long x;
    private final long y;
    private final double width;
    private final double height;

    @JsonProperty("last-modified")
    private Instant lastModified;
    @JsonProperty("z-index")
    private Integer zIndex;

    public Widget(UUID uuid, long x, long y, double width, double height,
                  @JsonProperty("last-modified") Instant lastModified,
                  @JsonProperty("z-index") Integer zIndex) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.lastModified = lastModified;
        this.zIndex = zIndex;
    }

    public Widget(UUID uuid, long x, long y, double width, double height, Integer zIndex) {
        this(uuid, x, y, width, height, Instant.now(), zIndex);
    }


    @Override
    public int hashCode() {
        return Objects.hash(uuid, x, y, width, height, zIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Widget)) return false;
        final Widget that = (Widget) obj;
        return
            this.uuid.equals(that.uuid) &&
            this.x == that.x &&
            this.y == that.y &&
            Double.compare(this.width, that.width) == 0 &&
            Double.compare(this.height, that.height) == 0 &&
            this.zIndex.equals(that.zIndex);
    }

    @Override
    public String toString() {
        return String.format("[u:%s, x:%d, y:%d, w:%.2f, h:%.2f, z:%d]",
                uuid, x, y, width, height, zIndex);
    }

    public UUID getUUID() {
        return uuid;
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

    public Instant getLastModified() {
        return lastModified;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
        this.lastModified = Instant.now();
    }

    public Integer getZIndex() {
        return zIndex;
    }
}
