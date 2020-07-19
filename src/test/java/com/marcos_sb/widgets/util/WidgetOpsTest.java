package com.marcos_sb.widgets.util;

import com.marcos_sb.widgets.resource.WidgetMutationSpec;
import com.marcos_sb.widgets.resource.Widget;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WidgetOpsTest {

    private Widget widget;

    @BeforeEach
    void initWidget() {
        widget = new Widget(UUID.randomUUID(), 1L, 2L, 3D, 4D, 0);
    }

    @Test
    @DisplayName("wrong uuid throws exception")
    void wrongUUID() {
        UUID uuid;
        do { uuid = UUID.randomUUID(); } while (widget.getUUID().equals(uuid));
        final WidgetMutationSpec mutation =
            new WidgetMutationSpec(uuid, null, null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> WidgetOps.update(widget, mutation));
    }

    @Test
    @DisplayName("identity update produces same widget")
    void identityUpdate() {
        final WidgetMutationSpec identityMutation =
            new WidgetMutationSpec(widget.getUUID(), null, null, null, null, null);
        final Widget updatedWidget = WidgetOps.update(widget, identityMutation);
        assertEquals(widget, updatedWidget);
    }

    @Test
    @DisplayName("update some properties")
    void updateSomeProperties() {
        final double newWidth = 2.1D;
        final int newZIndex = 2;
        final WidgetMutationSpec mutation =
            new WidgetMutationSpec(widget.getUUID(), null, null, newWidth, null, newZIndex);
        final Widget expectedWidget = new Widget(widget.getUUID(), widget.getX(),
            widget.getY(), newWidth, widget.getHeight(), newZIndex);
        final Widget actualWidget = WidgetOps.update(widget, mutation);

        assertEquals(expectedWidget, actualWidget);
    }

    @Test
    @DisplayName("update all properties")
    void updateAllProperties() {
        final long newX = 20L;
        final long newY = 21L;
        final double newWidth = 20.1D;
        final double newHeight = 21.1D;
        final int newZIndex = 23;
        final WidgetMutationSpec mutation =
            new WidgetMutationSpec(widget.getUUID(), newX, newY, newWidth, newHeight, newZIndex);
        final Widget expectedWidget =
            new Widget(widget.getUUID(), newX, newY, newWidth, newHeight, newZIndex);
        final Widget actualWidget = WidgetOps.update(widget, mutation);

        assertEquals(expectedWidget, actualWidget);
    }
}
