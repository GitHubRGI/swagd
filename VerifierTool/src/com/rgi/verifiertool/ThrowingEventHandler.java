package com.rgi.verifiertool;

import javafx.event.Event;
import javafx.event.EventHandler;

/**
 * Handler for events of a specific class / type, that may throw.
 *
 * @param <T> the event class this handler can handle
 *
 * @author Luke.Lambert
 */
@FunctionalInterface
public interface ThrowingEventHandler<T extends Event> extends EventHandler<T>
{
    @Override
    public default void handle(final T event)
    {
        try
        {
            handleThrows(event);
        }
        catch(final Throwable th)
        {
            throw new RuntimeException(th);
        }
    }

    /**
     * Invoked when a specific event of the type for which this handler is
     * registered happens.
     *
     * @param event the event which occurred
     *
     * @throws Throwable
     *             when the underlying handle throws
     */
    public void handleThrows(T event) throws Throwable;
}
