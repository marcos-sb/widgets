package com.marcos_sb.widgets;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("widgets")
@RequestMapping(value = "/widgets")
public class WidgetController {

    private final WidgetManager widgetManager;

    public WidgetController(WidgetManager widgetManager) {
        this.widgetManager = widgetManager;
    }

    public WidgetController() {
        this(new WidgetManager());
    }

    @PostMapping(value = "/new", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Widget> createWidget(
        @RequestBody FullWidgetSpec fullWidgetSpec
    ) throws WidgetManagerException {
        return ResponseEntity.ok(widgetManager.create(fullWidgetSpec));
    }

    @GetMapping(value = "/{uuid}", produces = "application/json")
    public ResponseEntity<Widget> getWidget(@PathVariable UUID uuid) throws WidgetManagerException {
        return ResponseEntity.ok(widgetManager.get(uuid));
    }

    @GetMapping(value = "/list/all", produces = "application/json")
    public ResponseEntity<List<Widget>> getAllWidgets() throws WidgetManagerException {
        return ResponseEntity.ok(widgetManager.getAllByZIndex());
    }

    @PutMapping(value = "/update", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Widget> updateWidget(
        @RequestBody WidgetMutationSpec widgetMutationSpec
    ) throws WidgetManagerException {
        return ResponseEntity.ok(widgetManager.update(widgetMutationSpec));
    }

    @DeleteMapping("/delete/{uuid}")
    public ResponseEntity deleteWidget(@PathVariable UUID uuid) {
        return ResponseEntity.ok().build();
    }
}
