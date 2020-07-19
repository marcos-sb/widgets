package com.marcos_sb.widgets.model.impl;

import com.marcos_sb.widgets.resource.NewWidgetSpec;
import com.marcos_sb.widgets.resource.Widget;
import com.marcos_sb.widgets.resource.WidgetMutationSpec;
import com.marcos_sb.widgets.exception.WidgetManagerException;
import com.marcos_sb.widgets.model.WidgetManager;
import com.marcos_sb.widgets.util.WidgetOps;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a thread-safe in-memory {@link WidgetManager}
 * to handle various operations on {@link Widget}s. The consistency guarantees
 * provided by this implementation include those specified in the
 * {@link WidgetManager} interface, namely UUID and z-index uniqueness, and
 * atomic updates to the {@link Widget}'s properties.
 * </p>
 * This class has been designed making no assumptions regarding the query
 * pattern of client applications, i.e. no one method is expected to be invoked
 * more often than any other.
 * </p>
 * This implementation provides thread-safety to client classes using two-level
 * locking with a {@link ReentrantLock} to protect the critical section in
 * state-mutating operations, and a {@link ReentrantReadWriteLock} to maintain the
 * consistency requirements while updating the {@link Widget}'s z-index
 * in case of overlapping z-indexes -- otherwise, reading operations may observe a state
 * that's missing {@link Widget}s that another thread is shifting. This thread-locking
 * approach has been implemented to reduce blocking as much as possible.
 * </p>
 * Additionally, this class relies on the concurrency mechanics provided by
 * the {@link ConcurrentHashMap} for average-case constant-time {@link Widget}
 * look-ups, and the {@link ConcurrentSkipListSet} for logarithmic-time z-index look-ups,
 * and thread-safe traversals of the {@Widget} set.
 *
 * @see WidgetManager
 */
public class BlockingWidgetManager implements WidgetManager {

    private static final int zIndexStep = 10;
    private static Logger logger = LoggerFactory.getLogger(BlockingWidgetManager.class);

    private final ConcurrentMap<UUID, Widget> uuid2widget;
    private final ConcurrentSkipListSet<Widget> widgets;
    private final ReentrantLock lock;
    private final ReentrantReadWriteLock rwLock;

    public BlockingWidgetManager(ConcurrentMap<UUID, Widget> uuid2widget,
                                 ConcurrentSkipListSet<Widget> widgets) {
        this.uuid2widget = uuid2widget;
        this.widgets = widgets;
        this.lock = new ReentrantLock(true);
        this.rwLock = new ReentrantReadWriteLock(true);
    }

    public BlockingWidgetManager() {
        this(new ConcurrentHashMap<>(),
             new ConcurrentSkipListSet<>(Comparator.comparingInt(Widget::getZIndex)));
    }

    @Override
    public Widget create(NewWidgetSpec newWidgetSpec) throws WidgetManagerException {
        try {
            lock.lock();

            UUID uuid;
            do { uuid = UUID.randomUUID(); } while (uuid2widget.containsKey(uuid));

            // If the widget spec specifies a z-index value, existing widgets may
            // need to be shifted up.
            Widget newWidget;
            if (newWidgetSpec.hasZIndex()) {
                newWidget = WidgetOps.widgetFrom(uuid, newWidgetSpec, newWidgetSpec.getzIndex());
                shiftOverlyingWidgetsUp(newWidget);
            } else {
                final int topZIndex = widgets.isEmpty() ? 0 : widgets.last().getZIndex();
                final int newWidgetZIndex = topZIndex + zIndexStep;
                newWidget = WidgetOps.widgetFrom(uuid, newWidgetSpec, newWidgetZIndex);
            }

            uuid2widget.put(uuid, newWidget);
            widgets.add(newWidget);

            return newWidget;
        } catch (Exception ex) {
            throw new WidgetManagerException(
                String.format("An error occurred while creating a new widget '%s'", newWidgetSpec), ex);
        } finally {
            lock.unlock();
        }
    }

    private void shiftOverlyingWidgetsUp(Widget widget) {
        final Widget floorWidget = widgets.floor(widget);
        if (floorWidget != null && floorWidget.getZIndex() == widget.getZIndex()) {
            final List<Widget> toIncZIndex = new ArrayList<>();

            // Finds first gap between widgets above 'newWidget'
            // and keeps track of all back-to-back widgets starting from 'floorWidget',
            // as they will have to be shifted up to open a gap for 'newWidget'
            Widget prev = floorWidget;
            Widget current;
            while (true) {
                toIncZIndex.add(prev);
                current = widgets.higher(prev);
                if (current != null && current.getZIndex() == prev.getZIndex() + 1)
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

    @Override
    public Widget get(UUID uuid) throws WidgetManagerException {
        try {
            rwLock.readLock().lock();
            if (!uuid2widget.containsKey(uuid))
                throw new NoSuchElementException(
                    String.format("Widget with uuid '%s' not found", uuid));

            return uuid2widget.get(uuid);
        } catch (NoSuchElementException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new WidgetManagerException(
                String.format("An error occurred while getting a widget, uuid '%s'", uuid), ex);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
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

    @Override
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
                WidgetOps.update(oldWidget, widgetMutationSpec);

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
            if (rwLock.isWriteLockedByCurrentThread())
                rwLock.writeLock().unlock();
            lock.unlock();
        }
    }

    @Override
    public Widget remove(UUID uuid) throws WidgetManagerException {
        try {
            lock.lock();

            if (!uuid2widget.containsKey(uuid))
                throw new NoSuchElementException(
                    String.format("Widget with uuid '%s' not found", uuid));

            widgets.remove(uuid2widget.get(uuid));
            return uuid2widget.remove(uuid);
        } catch (NoSuchElementException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new WidgetManagerException(
                String.format("An error occurred while removing a widget, uuid '%s'", uuid), ex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return uuid2widget.isEmpty() && widgets.isEmpty();
    }

    @Override
    public int size() {
        return uuid2widget.size();
    }
}
