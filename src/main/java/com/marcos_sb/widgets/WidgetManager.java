package com.marcos_sb.widgets;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WidgetManager {
    private static final int zIndexStep = 10;
    private static Logger logger = LoggerFactory.getLogger(WidgetManager.class);

    private final ConcurrentMap<UUID, Widget> uuid2widget;
    private final ConcurrentSkipListSet<Widget> widgets;
    private final ReentrantLock lock;
    private final ReentrantReadWriteLock rwLock;

    public WidgetManager() {
        this.uuid2widget = new ConcurrentHashMap<>();
        this.widgets = new ConcurrentSkipListSet<>(Comparator.comparingInt(Widget::getZIndex));
        this.lock = new ReentrantLock(true);
        this.rwLock = new ReentrantReadWriteLock(true);
    }

    public Widget create(FullWidgetSpec fullWidgetSpec) throws WidgetManagerException {
        try {
            lock.lock();

            UUID uuid;
            do {
                uuid = UUID.randomUUID();
            } while (uuid2widget.containsKey(uuid));

            // If the widget spec specifies a z-index value, existing widgets may
            // need to be shifted up.
            Widget newWidget;
            if (fullWidgetSpec.hasZIndex()) {
                newWidget = new Widget(uuid, fullWidgetSpec.getX(), fullWidgetSpec.getY(),
                    fullWidgetSpec.getWidth(), fullWidgetSpec.getHeight(), fullWidgetSpec.getzIndex());
                shiftOverlyingWidgetsUp(newWidget);
            } else {
                final int topZIndex = widgets.isEmpty() ? 0 : widgets.last().getZIndex();
                final int newWidgetZIndex = topZIndex + zIndexStep;
                newWidget = new Widget(uuid, fullWidgetSpec.getX(), fullWidgetSpec.getY(),
                    fullWidgetSpec.getWidth(), fullWidgetSpec.getHeight(), newWidgetZIndex);
            }

            uuid2widget.put(uuid, newWidget);
            widgets.add(newWidget);

            return newWidget;
        } catch (Exception ex) {
            throw new WidgetManagerException(
                String.format("An error occurred while creating a new widget '%s'", fullWidgetSpec), ex);
        } finally {
            lock.unlock();
        }
    }

    private void shiftOverlyingWidgetsUp(Widget widget) {
        final Widget floorWidget = widgets.floor(widget);
        if (floorWidget != null && floorWidget.getZIndex().equals(widget.getZIndex())) {
            final List<Widget> toIncZIndex = new ArrayList<>();

            // Finds first gap between widgets above 'newWidget'
            // and keeps track of all back-to-back widgets starting from 'floorWidget',
            // as they will have to be shifted up to open a gap for 'newWidget'
            Widget prev = floorWidget;
            Widget current;
            while (true) {
                toIncZIndex.add(prev);
                current = widgets.higher(prev);
                if (current != null && current.getZIndex().equals(prev.getZIndex() + 1))
                    prev = current;
                else break;
            }

            // Increments the z-index of all back-to-back widgets in-place, traversing
            // from highest to lowest z-index. This strategy will preserve the skip list
            // ordering invariant w/o forcing read ops to get a lock to observe this shifting
            // as atomic.
            // Different threads may observe different states of the skip list, but no snapshot
            // of the skip list will contain duplicate z-indexes.
            for (int i = toIncZIndex.size() - 1; i >= 0; i--) {
                final Widget w = toIncZIndex.get(i);
                w.setZIndex(w.getZIndex() + 1);
            }
        }
    }

    public Widget get(UUID uuid) throws WidgetManagerException {
        try {
            rwLock.readLock().lock();
            if (!uuid2widget.containsKey(uuid))
                throw new NoSuchElementException(
                    String.format("Widget with uuid '%s' not found", uuid));

            return uuid2widget.get(uuid);
        } catch (Exception ex) {
            throw new WidgetManagerException(
                String.format("An error occurred while getting a widget, uuid '%s'", uuid), ex);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public List<Widget> getAllByZIndex() throws WidgetManagerException {
        try {
            rwLock.readLock().lock();
            return new ArrayList<>(widgets);
        } catch (Exception ex) {
            throw new WidgetManagerException("An error occurred while getting the list of widgets", ex);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public Widget update(WidgetMutationSpec widgetMutationSpec) throws WidgetManagerException {
        try {
            lock.lock();

            final Instant now = Instant.now();
            final UUID uuid = widgetMutationSpec.getUUID();
            if (!uuid2widget.containsKey(uuid))
                throw new NoSuchElementException(
                    String.format("Widget with uuid '%s' not found", uuid));

            final Widget oldWidget = uuid2widget.get(uuid);
            if (now.isBefore(oldWidget.getLastModified())) {
                logger.info("Skipping stale update '{}'", widgetMutationSpec);
                return oldWidget;
            }
            final Widget newWidget =
                WidgetOps.updateWithSpec(oldWidget, widgetMutationSpec);

            // If there's no change to the z-index, no shifting is required.

            // If the z-index has been increased, only the overlying widgets
            // with the new z-index may need to be shifted.

            // If the z-index has been decreased, the underlying widgets
            // may need to be shifted. In this case, this shifting may
            // produce overlapping z-indexes between 'oldWidget' and the
            // widget immediately below 'oldWidget'.
            // Removing 'oldWidget', shifting and adding 'newWidget' solves the issue,
            // but any thread traversing the skip list may not see either
            // 'oldWidget' or 'newWidget'. Adding a RW lock is a possible solution.
            if (newWidget.getZIndex() < oldWidget.getZIndex()) {
                rwLock.writeLock().lock();
                widgets.remove(oldWidget);
                shiftOverlyingWidgetsUp(newWidget);
                uuid2widget.replace(uuid, newWidget);
                widgets.add(newWidget);
                return newWidget;
            }

            if (oldWidget.getZIndex() < newWidget.getZIndex())
                shiftOverlyingWidgetsUp(newWidget);

            uuid2widget.replace(uuid, newWidget);
            widgets.remove(oldWidget);
            widgets.add(newWidget);

            return newWidget;
        } catch (NoSuchElementException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new WidgetManagerException(
                String.format("An error occurred while updating a widget '%s'", widgetMutationSpec), ex);
        } finally {
            rwLock.writeLock().unlock();
            lock.unlock();
        }
    }

    public Widget remove(UUID uuid) throws WidgetManagerException {
        try {
            lock.lock();
            widgets.remove(uuid2widget.get(uuid));
            return uuid2widget.remove(uuid);
        } catch (Exception ex) {
            throw new WidgetManagerException(
                String.format("An error occurred while removing a widget, uuid '%s'", uuid), ex);
        } finally {
            lock.unlock();
        }
    }
}