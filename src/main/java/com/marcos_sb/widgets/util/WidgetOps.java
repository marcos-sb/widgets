package com.marcos_sb.widgets.util;

import com.marcos_sb.widgets.resource.NewWidgetSpec;
import com.marcos_sb.widgets.resource.WidgetMutationSpec;
import com.marcos_sb.widgets.resource.Widget;
import java.util.UUID;

public class WidgetOps {

    public static Widget update(Widget widget,
                                WidgetMutationSpec widgetMutationSpec) {
        if (!widget.getUUID().equals(widgetMutationSpec.getUUID()))
            throw new IllegalArgumentException(
                String.format("Cannot update widget. UUID mismatch (expected: '%s', actual '%s')",
                    widget.getUUID(), widgetMutationSpec.getUUID()));

        final long newX =
            widgetMutationSpec.getX() != null ? widgetMutationSpec.getX() : widget.getX();
        final long newY =
            widgetMutationSpec.getY() != null ? widgetMutationSpec.getY() : widget.getY();
        final double newWidth =
            widgetMutationSpec.getWidth() != null ? widgetMutationSpec.getWidth() : widget.getWidth();
        final double newHeight =
            widgetMutationSpec.getHeight() != null ? widgetMutationSpec.getHeight() : widget.getHeight();
        final Integer newZIndex =
            widgetMutationSpec.getZIndex() != null ? widgetMutationSpec.getZIndex() : widget.getZIndex();

        return new Widget(widget.getUUID(), newX, newY, newWidth, newHeight, newZIndex);
    }

    public static Widget widgetFrom(UUID uuid, NewWidgetSpec newWidgetSpec, int zIndex) {
        return new Widget(uuid, newWidgetSpec.getX(), newWidgetSpec.getY(),
            newWidgetSpec.getWidth(), newWidgetSpec.getHeight(), zIndex);
    }
}
