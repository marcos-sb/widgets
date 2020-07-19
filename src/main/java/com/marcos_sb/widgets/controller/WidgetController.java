package com.marcos_sb.widgets.controller;

import com.marcos_sb.widgets.resource.NewWidgetSpec;
import com.marcos_sb.widgets.resource.WidgetMutationSpec;
import com.marcos_sb.widgets.exception.WidgetManagerException;
import com.marcos_sb.widgets.model.WidgetManager;
import com.marcos_sb.widgets.model.impl.BlockingWidgetManager;
import com.marcos_sb.widgets.resource.Widget;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/widgets")
public class WidgetController {

    private final WidgetManager widgetManager;

    public WidgetController(WidgetManager widgetManager) {
        this.widgetManager = widgetManager;
    }

    public WidgetController() {
        this(new BlockingWidgetManager());
    }

    @PostMapping( value = "/new",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Widget> createWidget(
        @Valid @RequestBody NewWidgetSpec newWidgetSpec
    ) throws WidgetManagerException {
        return ResponseEntity.ok(widgetManager.create(newWidgetSpec));
    }

    @GetMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Widget> getWidget(@PathVariable UUID uuid) throws WidgetManagerException {
        return ResponseEntity.ok(widgetManager.get(uuid));
    }

    @GetMapping(value = "/list/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Widget>> getAllWidgets() throws WidgetManagerException {
        return ResponseEntity.ok(widgetManager.getAllByZIndex());
    }

    @PutMapping(value = "/update",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Widget> updateWidget(
        @Valid @RequestBody WidgetMutationSpec widgetMutationSpec
    ) throws WidgetManagerException {
        return ResponseEntity.ok(widgetManager.update(widgetMutationSpec));
    }

    @DeleteMapping(value = "/delete/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteWidget(@PathVariable UUID uuid) throws WidgetManagerException {
        return ResponseEntity.ok(widgetManager.remove(uuid));
    }
}
