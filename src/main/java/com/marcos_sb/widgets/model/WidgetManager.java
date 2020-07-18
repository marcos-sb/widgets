package com.marcos_sb.widgets.model;

import com.marcos_sb.widgets.api.json.WidgetMutationSpec;
import com.marcos_sb.widgets.api.json.NewWidgetSpec;
import com.marcos_sb.widgets.exception.WidgetManagerException;
import com.marcos_sb.widgets.model.impl.Widget;
import java.util.List;
import java.util.UUID;

public interface WidgetManager {
    Widget create(NewWidgetSpec newWidgetSpec) throws WidgetManagerException;

    Widget get(UUID uuid) throws WidgetManagerException;

    List<Widget> getAllByZIndex() throws WidgetManagerException;

    Widget update(WidgetMutationSpec widgetMutationSpec) throws WidgetManagerException;

    Widget remove(UUID uuid) throws WidgetManagerException;

    boolean isEmpty();

    int size();
}
