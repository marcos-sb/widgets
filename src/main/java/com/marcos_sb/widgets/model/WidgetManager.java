package com.marcos_sb.widgets.model;

import com.marcos_sb.widgets.resource.NewWidgetSpec;
import com.marcos_sb.widgets.resource.WidgetMutationSpec;
import com.marcos_sb.widgets.exception.WidgetManagerException;
import com.marcos_sb.widgets.resource.Widget;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * An interface that defines expected functionality from any implementing
 * Widget Manager.
 * </p>
 * Any implementing class should be thread-safe. The consistency requirements
 * have been relaxed so that clients of a {@code WidgetManager} implementation
 * could observe partial updates to the set of {@link Widget}s. Specifically,
 * [1] no duplicate {@link UUID}s and [2] no duplicate z-index should be visible by
 * clients.
 */
public interface WidgetManager {
    /**
     * Creates a new widget using the input specification.
     *
     * @param newWidgetSpec Desired properties for the new widget.
     * @return A new {@link Widget} initialized with the arguments supplied
     * in the specification.
     * @throws WidgetManagerException If it was not possible to create a new widget.
     */
    Widget create(NewWidgetSpec newWidgetSpec) throws WidgetManagerException;

    /**
     * Returns the only {@link Widget} in storage with the input {@link UUID}.
     * If there's no such {@link Widget} the implementation class should raise
     * a {@link NoSuchElementException}.
     *
     * @param uuid {@link UUID} of the widget to get the details from.
     * @return The {@link Widget} whose {@link UUID} matches the input argument.
     * @throws WidgetManagerException If it was not possible to retrieve
     * the {@link Widget}.
     * @throws NoSuchElementException If there's no {@link Widget} with the
     * {@link UUID} supplied.
     */
    Widget get(UUID uuid) throws WidgetManagerException;

    /**
     * Returns all {@link Widget}s in storage sorted by their z-index ascending.
     *
     * @return A list containing all stored {@link Widget}s sorted by their
     * z-index ascending.
     * @throws WidgetManagerException If it was not possible to retrieve and compose
     * the resulting list.
     */
    List<Widget> getAllByZIndex() throws WidgetManagerException;

    /**
     * Updates an existing {@link Widget} with the values provided in the
     * specification.
     *
     * @param widgetMutationSpec Contains the new values for the {@link Widget}.
     * @return The resulting {@link Widget} after applying the input mutation.
     * @throws WidgetManagerException If it was not possible to successfully update
     * the {@link Widget}.
     * @throws NoSuchElementException If there's no {@link Widget} in storage with the
     * {@link UUID} in the specification.
     */
    Widget update(WidgetMutationSpec widgetMutationSpec) throws WidgetManagerException;

    /**
     * Removes the {@link Widget} with the input {@link UUID}.
     *
     * @param uuid {@link UUID} of the {@link Widget} to delete.
     * @return The full details of the deleted {@link Widget}.
     * @throws WidgetManagerException If it was not possible to remove the
     * {@link Widget} from storage.
     * @throws NoSuchElementException If there exists no {@link Widget} with the
     * input {@link UUID}.
     */
    Widget remove(UUID uuid) throws WidgetManagerException;

    /**
     * @return true iff there's no {@link Widget} in storage.
     */
    boolean isEmpty();

    /**
     * @return the number of {@link Widget}s stored.
     */
    int size();
}
