package com.marcos_sb.widgets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class WidgetManagerTest {

    private ConcurrentMap<UUID, Widget> uuid2widget;
    private ConcurrentSkipListSet<Widget> widgets;
    private WidgetManager widgetManager;

    @Nested
    @DisplayName("when new")
    class WhenNew {

        final WidgetMutationSpec nullMutation =
            new WidgetMutationSpec(UUID.randomUUID(), null, null, null, null, null);

        @BeforeEach
        void createNewWidgetManager() {
            uuid2widget = new ConcurrentHashMap<>();
            widgets = new ConcurrentSkipListSet<>(Comparator.comparingInt(Widget::getZIndex));
            widgetManager = new WidgetManager(uuid2widget, widgets);
        }

        @Test
        @DisplayName("is empty")
        void isEmpty() {
            assertAll("widgets",
                () -> assertTrue(uuid2widget::isEmpty),
                () -> assertTrue(widgets::isEmpty));
        }

        @Test
        @DisplayName("get any uuid throws exception")
        void getThrowsException() {
            assertThrows(NoSuchElementException.class, () -> widgetManager.get(nullMutation.getUUID()));
        }

        @Test
        @DisplayName("get all widgets returns an empty list")
        void returnsAnEmptyWidgetList() throws WidgetManagerException {
            assertTrue(widgetManager.getAllByZIndex().isEmpty());
        }

        @Test
        @DisplayName("update any widget throws exception")
        void updateThrowsException() {
            assertThrows(NoSuchElementException.class, () -> widgetManager.update(nullMutation));
        }

        @Test
        @DisplayName("remove any uuid throws exception")
        void removeThrowsException() {
            assertThrows(NoSuchElementException.class, () -> widgetManager.remove(nullMutation.getUUID()));
        }

        @Nested
        @DisplayName("after creating one widget")
        class AfterCreatingOne {

            final NewWidgetSpec widgetSpecZIndex = new NewWidgetSpec(0, 1, 2, 3, -1);
            Widget createdWidget;

            @BeforeEach
            void createOneWidget() throws WidgetManagerException {
                createdWidget = widgetManager.create(widgetSpecZIndex);
            }

            @Test
            @DisplayName("is not empty")
            void isNotEmpty() {
                assertAll("widgets",
                    () -> assertFalse(uuid2widget::isEmpty),
                    () -> assertFalse(widgets::isEmpty));
            }

            @Test
            @DisplayName("internal map and set of size 1")
            void mapAndSetOfSize1() {
                assertAll("widgets",
                    () -> assertEquals(1, uuid2widget.size()),
                    () -> assertEquals(1, widgets.size()));
            }

            @Test
            @DisplayName("get returns the widget created")
            void getCreatedWidget() throws WidgetManagerException {
                assertEquals(createdWidget, widgetManager.get(createdWidget.getUUID()));
            }

            @Test
            @DisplayName("get all returns one widget")
            void getAll() throws WidgetManagerException {
                assertEquals(Collections.singletonList(createdWidget), widgetManager.getAllByZIndex());
            }

            @Test
            @DisplayName("update returns the updated widget")
            void updatedWidget() throws WidgetManagerException {
                final WidgetMutationSpec mutationSpec =
                    new WidgetMutationSpec(createdWidget.getUUID(), 0L, 0L, 0D, 0D, 0);
                final Widget updatedWidget = WidgetOps.updateWithSpec(createdWidget, mutationSpec);
                assertEquals(updatedWidget, widgetManager.update(mutationSpec));
            }

            @Test
            @DisplayName("remove deletes the widget created")
            void removedWidget() throws WidgetManagerException {
                assertEquals(createdWidget, widgetManager.remove(createdWidget.getUUID()));
            }

            @Test
            @DisplayName("remove makes internal map and set empty")
            void removedWidgetIsEmpty() throws WidgetManagerException {
                widgetManager.remove(createdWidget.getUUID());
                assertAll("widgets",
                    () -> assertTrue(uuid2widget::isEmpty),
                    () -> assertTrue(widgets::isEmpty));
            }
        }
    }
}
