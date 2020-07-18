package com.marcos_sb.widgets.api.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import javax.validation.constraints.DecimalMin;

public class WidgetMutationSpec {

    @JsonProperty(required = true)
    private final UUID uuid;

    private final Long x;
    private final Long y;

    @DecimalMin("0")
    private final Double width;

    @DecimalMin("0")
    private final Double height;

    private final Integer zIndex;

    @JsonCreator
    public WidgetMutationSpec(UUID uuid, Long x, Long y, Double width, Double height, Integer zIndex) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zIndex = zIndex;
    }

    @Override
    public String toString() {
        return String.format("[u:%s, x:%d, y:%d, w:%.2f, h:%.2f, z:%d]",
            uuid, x, y, width, height, zIndex);
    }

    public UUID getUUID() {
        return uuid;
    }

    public Long getX() {
        return x;
    }

    public Long getY() {
        return y;
    }

    public Double getWidth() {
        return width;
    }

    public Double getHeight() {
        return height;
    }

    public Integer getZIndex() {
        return zIndex;
    }

    @JsonIgnore
    public boolean hasZIndex() {
        return zIndex != null;
    }
}
