package com.marcos_sb.widgets.model.impl;

import com.marcos_sb.widgets.resource.NewWidgetSpec;
import com.marcos_sb.widgets.resource.Widget;
import com.marcos_sb.widgets.resource.WidgetMutationSpec;
import com.marcos_sb.widgets.exception.WidgetManagerException;
import com.marcos_sb.widgets.util.WidgetOps;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockingWidgetManagerTest {

    private BlockingWidgetManager widgetManager;

    @Nested
    @DisplayName("when new")
    class WhenNew {

        final WidgetMutationSpec nullMutation =
            new WidgetMutationSpec(UUID.randomUUID(), null, null, null, null, null);

        @BeforeEach
        void createNewWidgetManager() {
            widgetManager = new BlockingWidgetManager();
        }

        @Test
        @DisplayName("is empty")
        void isEmpty() {
            assertTrue(widgetManager.isEmpty());
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
        class AfterCreatingOneWidget {

            final NewWidgetSpec widgetSpecZIndex = new NewWidgetSpec(0, 1, 2, 3, -1);
            Widget createdWidget;

            @BeforeEach
            void createOneWidget() throws WidgetManagerException {
                createdWidget = widgetManager.create(widgetSpecZIndex);
            }

            @Test
            @DisplayName("has size 1")
            void sizeOf1() {
                assertEquals(1, widgetManager.size());
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
                final Widget updatedWidget = WidgetOps.update(createdWidget, mutationSpec);
                assertEquals(updatedWidget, widgetManager.update(mutationSpec));
            }

            @Test
            @DisplayName("remove deletes the widget created")
            void removedWidget() throws WidgetManagerException {
                assertEquals(createdWidget, widgetManager.remove(createdWidget.getUUID()));
            }

            @Test
            @DisplayName("remove makes it empty")
            void removedWidgetIsEmpty() throws WidgetManagerException {
                widgetManager.remove(createdWidget.getUUID());
                assertTrue(widgetManager.isEmpty());
            }
        }

        @Nested
        @DisplayName("after creating two widgets")
        class AfterCreatingTwoWidgets {

            final NewWidgetSpec widgetSpecZIndex1 = new NewWidgetSpec(0, 1, 2, 3, -1);
            final NewWidgetSpec widgetSpecZIndex2 = new NewWidgetSpec(0, 0, 0, 0, 0);
            Widget createdWidgetZIndex1;
            Widget createdWidgetZIndex2;

            @BeforeEach
            void createTwoWidgets() throws WidgetManagerException {
                createdWidgetZIndex1 = widgetManager.create(widgetSpecZIndex1);
                createdWidgetZIndex2 = widgetManager.create(widgetSpecZIndex2);
            }

            @Test
            @DisplayName("create new w/o z-index appears on top")
            void createEmptyZIndexOnTop() throws WidgetManagerException {
                final NewWidgetSpec widgetSpecNoZIndex = new NewWidgetSpec(0, 1, 2, 3);
                final Widget createdWidgetNoZIndex = widgetManager.create(widgetSpecNoZIndex);
                final List<Widget> allWidgets =
                    Arrays.asList(createdWidgetZIndex1, createdWidgetZIndex2, createdWidgetNoZIndex);

                assertEquals(allWidgets, widgetManager.getAllByZIndex());
            }

            @Test
            @DisplayName("create new w/ existing z-index shifts up")
            void createWithZIndexShifts() throws WidgetManagerException {
                final NewWidgetSpec widgetSpecExistingZIndex = new NewWidgetSpec(0, 1, 2, 3, -1);
                final Widget createdWidgetExistingZIndex = widgetManager.create(widgetSpecExistingZIndex);
                final List<Widget> allWidgets =
                    Arrays.asList(createdWidgetExistingZIndex, createdWidgetZIndex1, createdWidgetZIndex2);

                assertEquals(allWidgets, widgetManager.getAllByZIndex());
            }

            @Test
            @DisplayName("get all returns list in ascending z-index")
            void getAll() throws WidgetManagerException {
                final List<Widget> allWidgets =
                    Arrays.asList(createdWidgetZIndex1, createdWidgetZIndex2);
                assertEquals(allWidgets, widgetManager.getAllByZIndex());
            }

            @Test
            @DisplayName("update z-index top-to-bottom")
            void updateZIndexTopToBottom() throws WidgetManagerException {
                final WidgetMutationSpec mutateWidget2BelowWidget1 =
                    new WidgetMutationSpec(createdWidgetZIndex2.getUUID(), null, null, null, null, -2);
                final Widget mutatedWidget2 = widgetManager.update(mutateWidget2BelowWidget1);
                final List<Widget> allWidgets = Arrays.asList(mutatedWidget2, createdWidgetZIndex1);

                assertEquals(allWidgets, widgetManager.getAllByZIndex());
            }

            @Test
            @DisplayName("update z-index top-to-bottom overlapping z-index")
            void updateZIndexTopToBottomOverlap() throws WidgetManagerException {
                final WidgetMutationSpec mutateWidget2OverlapWidget1 =
                    new WidgetMutationSpec(createdWidgetZIndex2.getUUID(), null, null, null, null, -1);
                final Widget mutatedWidget2 = widgetManager.update(mutateWidget2OverlapWidget1);
                final List<Widget> allWidgets = Arrays.asList(mutatedWidget2, createdWidgetZIndex1);

                assertEquals(allWidgets, widgetManager.getAllByZIndex());
            }
        }
    }
}
